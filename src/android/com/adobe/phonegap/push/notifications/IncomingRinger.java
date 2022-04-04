package com.adobe.phonegap.push.notifications;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adobe.phonegap.push.logs.Logger;
import com.adobe.phonegap.push.utils.RingtoneUtil;
import com.adobe.phonegap.push.utils.ServiceUtils;

import java.io.IOException;

/**
 * This class is used to make the mobile play a ringtone sound and vibrate
 * while a call request is being shown.
 * Use this class a workaround to play ringtone and vibration,
 * as I wasn't able to find a reliable way to determine
 * vibration or ringtone enabled status, specially in Xiaomi devices
 */
public class IncomingRinger {

    private static final String TAG = "IncomingRinger";

    private static final long[] VIBRATE_PATTERN = {0, 1000, 1000};

    private final Context  context;
    private final Vibrator vibrator;

    private MediaPlayer player;

    IncomingRinger(Context context) {
        this.context  = context.getApplicationContext();
        this.vibrator = ServiceUtils.getVibratorService();
    }

    public void start(@Nullable Uri uri, boolean vibrate) {
        AudioManager audioManager = ServiceUtils.getAudioService();

        if (player != null) {
            player.release();
        }

        if (uri != null) {
            player = createPlayer(uri);
        }

        int ringerMode = audioManager.getRingerMode();

        if (shouldVibrate(context, player, ringerMode, vibrate)) {
            Logger.Debug(TAG, "Vibration", "Starting");
            vibrator.vibrate(VIBRATE_PATTERN, 1);
        } else {
            Logger.Debug(TAG, "Vibration", "Skipping");
        }

        if (player != null && ringerMode == AudioManager.RINGER_MODE_NORMAL) {
            try {
                if (!player.isPlaying()) {
                    player.prepare();
                    player.start();
                    Logger.Debug(TAG, "Ringtone", "Playing ringtone now...");
                } else {
                    Logger.Debug(TAG, "Ringtone", "Ringtone is already playing, declining to restart.");
                }
            } catch (IllegalStateException | IOException e) {
                Logger.Error(TAG, "Ringtone", e.toString());
                e.printStackTrace();
                player = null;
            }
        } else {
            Logger.Debug(TAG, "Ringtone", "Not ringing, player: " + (player != null ? "available" : "null") + " modeInt: " + ringerMode + " mode: " + (ringerMode == AudioManager.RINGER_MODE_SILENT ? "silent" : "vibrate only"));
        }
    }

    public void stop() {
        if (player != null) {
            Logger.Debug(TAG, "Ringtone", "Stopping ringer");
            player.release();
            player = null;
        }

        Logger.Debug(TAG, "Vibration", "Cancelling vibrator");
        vibrator.cancel();
    }

    private boolean shouldVibrate(Context context, MediaPlayer player, int ringerMode, boolean vibrate) {
        if (player == null) {
            return true;
        }

        Vibrator vibrator = ServiceUtils.getVibratorService();

        if (vibrator == null || !vibrator.hasVibrator()) {
            return false;
        }

        if (vibrate) {
            return ringerMode != AudioManager.RINGER_MODE_SILENT;
        } else {
            return ringerMode == AudioManager.RINGER_MODE_VIBRATE;
        }
    }

    private @Nullable MediaPlayer createPlayer(@NonNull Uri ringtoneUri) {
        try {
            MediaPlayer mediaPlayer = safeCreatePlayer(ringtoneUri);

            if (mediaPlayer == null) {
                Logger.Debug(TAG, "createPlayer", "Failed to create player for incoming call ringer due to custom rom most likely");
                return null;
            }

            mediaPlayer.setOnErrorListener(new MediaPlayerErrorListener());
            mediaPlayer.setLooping(true);

            if (Build.VERSION.SDK_INT <= 21) {
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
            } else {
                mediaPlayer.setAudioAttributes(new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .build());
            }

            return mediaPlayer;
        } catch (IOException e) {
            Logger.Debug(TAG, "createPlayer", "Failed to create player for incoming call ringer: " + e);
            return null;
        }
    }

    private @Nullable MediaPlayer safeCreatePlayer(@NonNull Uri ringtoneUri) throws IOException {
        try {
            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(context, ringtoneUri);
            return mediaPlayer;
        } catch (SecurityException e) {
            Logger.Error(TAG, "safeCreatePlayer", "Failed to create player with ringtone the normal way: " + e);
        }

        if (ringtoneUri.equals(Settings.System.DEFAULT_RINGTONE_URI)) {
            try {
                Uri defaultRingtoneUri = RingtoneUtil.getActualDefaultRingtoneUri(context);
                if (defaultRingtoneUri != null) {
                    MediaPlayer mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDataSource(context, defaultRingtoneUri);
                    return mediaPlayer;
                }
            } catch (SecurityException e) {
                Logger.Debug(TAG, "safeCreatePlayer", "Failed to set default ringtone with fallback approach: " + e);
            }
        }

        return null;
    }

    private class MediaPlayerErrorListener implements MediaPlayer.OnErrorListener {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            Logger.Debug(TAG, "MediaPlayerErrorListener", "onError(" + mp + ", " + what + ", " + extra);
            player = null;
            return false;
        }
    }

}

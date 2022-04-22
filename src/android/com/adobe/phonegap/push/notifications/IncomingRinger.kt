package com.adobe.phonegap.push.notifications

import android.content.Context
import com.adobe.phonegap.push.logs.Logger.Debug
import com.adobe.phonegap.push.logs.Logger.Error
import android.os.Vibrator
import android.media.MediaPlayer
import android.media.AudioManager
import com.adobe.phonegap.push.utils.ServiceUtils
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.provider.Settings
import androidx.annotation.RequiresApi
import kotlin.Throws
import com.adobe.phonegap.push.utils.RingtoneUtil
import java.io.IOException
import java.lang.IllegalStateException

/**
 * This class is used to make the mobile play a ringtone sound and vibrate
 * while a call request is being shown.
 * Use this class a workaround to play ringtone and vibration,
 * as I wasn't able to find a reliable way to determine
 * vibration or ringtone enabled status, specially in Xiaomi devices
 */
class IncomingRinger internal constructor(context: Context) {
    private val context: Context = context.applicationContext
    private val vibrator: Vibrator = ServiceUtils.vibratorService(context)
    private var player: MediaPlayer? = null
    @RequiresApi(Build.VERSION_CODES.O)
    fun start(uri: Uri?, vibrate: Boolean) {
        val audioManager = ServiceUtils.audioService(context)
        if (player != null) {
            player!!.release()
        }
        if (uri != null) {
            player = createPlayer(uri)
        }
        val ringerMode = audioManager.ringerMode
        if (shouldVibrate(context, player, ringerMode, vibrate)) {
            Debug(TAG, "Vibration", "Starting")
            vibrator.vibrate(VibrationEffect.createWaveform(VIBRATE_PATTERN, 1))
        } else {
            Debug(TAG, "Vibration", "Skipping")
        }
        if (player != null && ringerMode == AudioManager.RINGER_MODE_NORMAL) {
            try {
                if (!player!!.isPlaying) {
                    player!!.prepare()
                    player!!.start()
                    Debug(TAG, "Ringtone", "Playing ringtone now...")
                } else {
                    Debug(TAG, "Ringtone", "Ringtone is already playing, declining to restart.")
                }
            } catch (e: IllegalStateException) {
                Error(TAG, "Ringtone", e.toString())
                e.printStackTrace()
                player = null
            } catch (e: IOException) {
                Error(TAG, "Ringtone", e.toString())
                e.printStackTrace()
                player = null
            }
        } else {
            Debug(
                TAG,
                "Ringtone",
                "Not ringing, player: " + (if (player != null) "available" else "null") + " modeInt: " + ringerMode + " mode: " + if (ringerMode == AudioManager.RINGER_MODE_SILENT) "silent" else "vibrate only"
            )
        }
    }

    fun stop() {
        if (player != null) {
            Debug(TAG, "Ringtone", "Stopping ringer")
            player!!.release()
            player = null
        }
        Debug(TAG, "Vibration", "Cancelling vibrator")
        vibrator.cancel()
    }

    private fun shouldVibrate(
        context: Context,
        player: MediaPlayer?,
        ringerMode: Int,
        vibrate: Boolean
    ): Boolean {
        if (player == null) {
            return true
        }
        val vibrator = ServiceUtils.vibratorService(context)
        if (!vibrator.hasVibrator()) {
            return false
        }
        return if (vibrate) {
            ringerMode != AudioManager.RINGER_MODE_SILENT
        } else {
            ringerMode == AudioManager.RINGER_MODE_VIBRATE
        }
    }

    private fun createPlayer(ringtoneUri: Uri): MediaPlayer? {
        return try {
            val mediaPlayer = safeCreatePlayer(ringtoneUri)
            if (mediaPlayer == null) {
                Debug(
                    TAG,
                    "createPlayer",
                    "Failed to create player for incoming call ringer due to custom rom most likely"
                )
                return null
            }
            mediaPlayer.setOnErrorListener(MediaPlayerErrorListener())
            mediaPlayer.isLooping = true
            mediaPlayer.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .build()
            )
            mediaPlayer
        } catch (e: IOException) {
            Debug(TAG, "createPlayer", "Failed to create player for incoming call ringer: $e")
            null
        }
    }

    @Throws(IOException::class)
    private fun safeCreatePlayer(ringtoneUri: Uri): MediaPlayer? {
        try {
            val mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(context, ringtoneUri)
            return mediaPlayer
        } catch (e: SecurityException) {
            Error(
                TAG,
                "safeCreatePlayer",
                "Failed to create player with ringtone the normal way: $e"
            )
        }
        if (ringtoneUri == Settings.System.DEFAULT_RINGTONE_URI) {
            try {
                val defaultRingtoneUri = RingtoneUtil.getActualDefaultRingtoneUri(context)
                if (defaultRingtoneUri != null) {
                    val mediaPlayer = MediaPlayer()
                    mediaPlayer.setDataSource(context, defaultRingtoneUri)
                    return mediaPlayer
                }
            } catch (e: SecurityException) {
                Debug(
                    TAG,
                    "safeCreatePlayer",
                    "Failed to set default ringtone with fallback approach: $e"
                )
            }
        }
        return null
    }

    private inner class MediaPlayerErrorListener : MediaPlayer.OnErrorListener {
        override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
            Debug(TAG, "MediaPlayerErrorListener", "onError($mp, $what, $extra")
            player = null
            return false
        }
    }

    companion object {
        private const val TAG = "IncomingRinger"
        private val VIBRATE_PATTERN = longArrayOf(0, 1000, 1000)
    }

}
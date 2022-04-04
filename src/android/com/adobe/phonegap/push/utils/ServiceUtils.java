package com.adobe.phonegap.push.utils;

import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioManager;
import android.os.PowerManager;
import android.os.Vibrator;

public class ServiceUtils {

    public static PowerManager getPowerService() {
        return (PowerManager) Globals.applicationContext.getSystemService(Context.POWER_SERVICE);
    }

    public static NotificationManager getNotificationService() {
        return (NotificationManager) Globals.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static Vibrator getVibratorService() {
        return (Vibrator) Globals.applicationContext.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public static AudioManager getAudioService() {
        return (AudioManager) Globals.applicationContext.getSystemService(Context.AUDIO_SERVICE);
    }
}

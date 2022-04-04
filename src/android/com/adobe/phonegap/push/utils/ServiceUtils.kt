package com.adobe.phonegap.push.utils

import android.os.PowerManager
import android.app.NotificationManager
import android.content.Context
import android.os.Vibrator
import android.media.AudioManager

object ServiceUtils {
    fun powerService(context: Context): PowerManager {
        return context.getSystemService(Context.POWER_SERVICE) as PowerManager
    }
    fun notificationService(context: Context): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
    fun vibratorService(context: Context): Vibrator {
        return context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    fun audioService(context: Context): AudioManager {
        return context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
}
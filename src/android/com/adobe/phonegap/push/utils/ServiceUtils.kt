package com.adobe.phonegap.push.utils

import android.os.PowerManager
import android.app.NotificationManager
import android.content.Context
import android.os.Vibrator
import android.media.AudioManager

object ServiceUtils {
    @JvmStatic
    val powerService: PowerManager
        get() = Globals.applicationContext!!.getSystemService(Context.POWER_SERVICE) as PowerManager
    val notificationService: NotificationManager
        get() = Globals.applicationContext!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val vibratorService: Vibrator
        get() = Globals.applicationContext!!.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    @JvmStatic
    val audioService: AudioManager
        get() = Globals.applicationContext!!.getSystemService(Context.AUDIO_SERVICE) as AudioManager
}
package com.adobe.phonegap.push.notifications.channels

import android.app.Notification
import android.media.AudioAttributes
import android.app.NotificationManager
import android.provider.Settings

interface ICustomChannel {
    companion object {
        const val CHANNEL_ID = ""
        const val PREFIX_ID = ""
        const val name = ""
        const val vibration = false
        val vibrationPattern = longArrayOf()
        const val visibility = Notification.VISIBILITY_PUBLIC
        val sound = Settings.System.DEFAULT_RINGTONE_URI
        val audioAttributes: AudioAttributes? = null
        const val light = -1
        const val showBadge = false
        const val priority = NotificationManager.IMPORTANCE_DEFAULT
    }
}
package com.adobe.phonegap.push.notifications.channels

import android.app.Notification
import android.os.Bundle
import android.media.AudioAttributes
import android.app.NotificationManager
import android.graphics.Color
import android.net.Uri
import com.adobe.phonegap.push.PushPlugin

class MessageChannel : ICustomChannel {
    fun asJSON(): Bundle {
        val json = Bundle()
        json.putString("id", PREFIX_ID)
        json.putString("name", name)
        json.putBoolean("vibration", vibration)
        json.putBoolean("showBadge", showBadge)
        json.putLongArray("vibrationPattern", vibrationPattern)
        json.putInt("visibility", visibility)
        json.putInt("light", light)
        json.putInt("priority", priority)
        json.putParcelable("sound", sound)
        json.putParcelable("audioAttributes", audioAttributes)
        return json
    }

    companion object {
        var CHANNEL_ID = ""
        const val PREFIX_ID = "Messages"
        var name = "Messages"
        const val vibration = true
        val vibrationPattern = longArrayOf(0, 500, 1000)
        const val visibility = Notification.VISIBILITY_PUBLIC
        val sound: Uri? = null
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()
        const val light = Color.BLUE
        const val showBadge = true
        const val priority = NotificationManager.IMPORTANCE_HIGH
    }

    init {
        when {
            PushPlugin.language === "es" -> {
                name = "Mensajes"
            }
            PushPlugin.language === "en" -> {
                name = "Messages"
            }
            PushPlugin.language === "ca" -> {
                name = "Missatges"
            }
            PushPlugin.language === "pt" -> {
                name = "Mensagens"
            }
        }
    }
}
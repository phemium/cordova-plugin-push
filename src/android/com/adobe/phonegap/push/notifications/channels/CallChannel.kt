package com.adobe.phonegap.push.notifications.channels

import android.app.Notification
import android.os.Bundle
import android.app.NotificationManager
import android.graphics.Color
import com.adobe.phonegap.push.utils.Globals

class CallChannel : ICustomChannel {
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
        return json
    }

    companion object {
        @JvmField
        var CHANNEL_ID = ""
        const val PREFIX_ID = "VoIP"
        var name = "Calls"
        const val vibration = true
        val vibrationPattern = longArrayOf(0, 500, 1000)
        const val visibility = Notification.VISIBILITY_PUBLIC
        const val light = Color.GREEN
        const val showBadge = true
        const val priority = NotificationManager.IMPORTANCE_MAX
    }

    init {
        if (Globals.language === "es") {
            name = "Llamadas"
        } else if (Globals.language === "en") {
            name = "Calls"
        } else if (Globals.language === "ca") {
            name = "Trucades"
        } else if (Globals.language === "pt") {
            name = "Chamadas"
        }
    }
}
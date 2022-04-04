package com.adobe.phonegap.push.notifications

import com.adobe.phonegap.push.logs.Logger.Debug
import com.adobe.phonegap.push.logs.Logger.setDebug
import com.adobe.phonegap.push.logs.Logger.Error
import com.adobe.phonegap.push.PushPlugin
import androidx.annotation.RequiresApi
import android.os.Build
import android.app.NotificationChannel
import com.adobe.phonegap.push.utils.ServiceUtils
import android.app.NotificationManager
import android.os.Bundle
import android.media.AudioAttributes
import android.net.Uri
import java.util.*

object NotificationChannelManager {
    private val TAG = PushPlugin.PREFIX_TAG + NotificationChannelManager::class.java

    @get:RequiresApi(api = Build.VERSION_CODES.O)
    val list: List<NotificationChannel>
        get() = ServiceUtils.notificationService.notificationChannels

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun getOne(channelId: String?): NotificationChannel {
        val notificationManager = ServiceUtils.notificationService
        return notificationManager.getNotificationChannel(channelId)
    }

    /**
     * Automatically creates a Notification Channel with the given specs
     * @param force
     */
    fun createIfNeeded(params: Bundle, force: Boolean): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Debug(TAG, "createIfNeeded", "Channel: " + params.getString("id"))
            val notificationManager = ServiceUtils.notificationService
            val notificationChannels = notificationManager.notificationChannels
            var foundId: String? = null
            for (notificationChannel in notificationChannels) {
                val channelId = notificationChannel.id
                if (channelId.startsWith(params.getString("id")!!)) {
                    foundId = channelId
                    if (force) {
                        foundId = null
                        delete(channelId)
                    }
                }
            }
            if (foundId != null) {
                setDebug(true)
                Debug(
                    TAG,
                    "createIfNeeded",
                    "Channel is found and not forced to be recreated: $foundId"
                )
                foundId
            } else {
                setDebug(true)
                Debug(TAG, "createIfNeeded", "Creating channel as new...")
                val newChannelId = params.getString("id") + Date().time
                Debug(TAG, "createIfNeeded", "ChannelID: $newChannelId")
                // Set ID, Name and Importance
                val notificationchannel = NotificationChannel(
                    newChannelId,
                    params.getString("name"),
                    params.getInt("priority")
                )
                // Set Visibility
                notificationchannel.lockscreenVisibility = params.getInt("visibility")
                Debug(TAG, "createIfNeeded", "visibility: " + params.getInt("visibility"))
                // Set Call Sound or not
                val sound = params.getParcelable<Uri>("sound")
                val audioAttributes = params.getParcelable<AudioAttributes>("audioAttributes")
                if (sound != null && audioAttributes != null) {
                    Debug(TAG, "createIfNeeded", "Setting sound")
                    notificationchannel.setSound(sound, audioAttributes)
                }
                // Set Vibration
                if (params.getBoolean("vibration")) {
                    Debug(
                        TAG,
                        "createIfNeeded",
                        "vibration: " + params.getLongArray("vibrationPattern")
                    )
                    notificationchannel.vibrationPattern = params.getLongArray("vibrationPattern")
                    notificationchannel.enableVibration(true)
                }
                // Set Lights
                val light = params.getInt("light")
                if (light != -1) {
                    Debug(TAG, "createIfNeeded", "light: $light")
                    notificationchannel.lightColor = light
                    notificationchannel.enableLights(true)
                }
                // Set Show Badge
                val showBadge = params.getBoolean("showBadge")
                notificationchannel.setShowBadge(showBadge)
                // Set priority
                notificationchannel.importance = NotificationManager.IMPORTANCE_HIGH
                // Create channel
                Debug(TAG, "createIfNeeded", "showBadge: $showBadge")
                notificationManager.createNotificationChannel(notificationchannel)
                newChannelId
            }
        } else {
            null
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun delete(channelId: String) {
        val mNotificationManager = ServiceUtils.notificationService
        if (mNotificationManager != null) {
            mNotificationManager.deleteNotificationChannel(channelId)
        } else {
            Error(TAG, "delete($channelId)", "failed")
        }
    }
}
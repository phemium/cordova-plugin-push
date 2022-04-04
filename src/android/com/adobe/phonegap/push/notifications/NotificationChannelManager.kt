package com.adobe.phonegap.push.notifications

import com.adobe.phonegap.push.logs.Logger.Debug
import com.adobe.phonegap.push.logs.Logger.setDebug
import com.adobe.phonegap.push.PushPlugin
import androidx.annotation.RequiresApi
import android.os.Build
import android.app.NotificationChannel
import com.adobe.phonegap.push.utils.ServiceUtils
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.media.AudioAttributes
import android.net.Uri
import com.adobe.phonegap.push.logs.Logger
import java.util.*

object NotificationChannelManager {
    private val TAG = PushPlugin.PREFIX_TAG + NotificationChannelManager::class.java

    fun list(context: Context): List<NotificationChannel> {
        return ServiceUtils.notificationService(context).notificationChannels
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun getOne(context: Context, channelId: String?): NotificationChannel {
        val notificationManager = ServiceUtils.notificationService(context)
        return notificationManager.getNotificationChannel(channelId)
    }

    /**
     * Automatically creates a Notification Channel with the given specs
     * @param force
     */
    fun createIfNeeded(context: Context, params: Bundle, force: Boolean): String {
        Debug(TAG, "createIfNeeded", "Channel: " + params.getString("id"))
        val notificationManager = ServiceUtils.notificationService(context)
        val notificationChannels = notificationManager.notificationChannels
        var foundId: String? = null
        for (notificationChannel in notificationChannels) {
            val channelId = notificationChannel.id
            if (channelId.startsWith(params.getString("id")!!)) {
                foundId = channelId
                if (force) {
                    foundId = null
                    delete(context, channelId)
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
            return foundId
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
            return newChannelId
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun delete(context: Context, channelId: String) {
        val mNotificationManager = ServiceUtils.notificationService(context)
        try {
            mNotificationManager.deleteNotificationChannel(channelId)
        } catch (e: SecurityException) {
            Logger.Error(TAG, "delete", "Executed from a foreground service (probably)")
        }
    }
}
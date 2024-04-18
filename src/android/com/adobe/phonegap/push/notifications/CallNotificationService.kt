package com.adobe.phonegap.push.notifications

import android.annotation.SuppressLint
import com.adobe.phonegap.push.logs.Logger.Debug
import android.content.Intent
import androidx.annotation.RequiresApi
import com.adobe.phonegap.push.PushConstants
import com.adobe.phonegap.push.notifications.channels.CallChannel
import android.media.RingtoneManager
import com.adobe.phonegap.push.utils.Tools
import android.app.PendingIntent.CanceledException
import kotlin.Throws
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.os.*
import com.adobe.phonegap.push.PushPlugin
import com.adobe.phonegap.push.notifications.channels.MessageChannel
import java.lang.Exception

class CallNotificationService : Service() {
    private var lostCallHandler: Handler? = null
    private var ringtone: IncomingRinger? = null
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        if (ringtone == null) {
            ringtone = IncomingRinger(this)
        }
    }

    override fun onDestroy() {
        isBusy = false
        if (ringtone != null) {
            ringtone!!.stop()
        }
        if (lostCallHandler != null) {
            lostCallHandler!!.removeCallbacksAndMessages(null)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        // Retrieve IntentExtras
        val data = intent.extras

        // Get consultation ID
        val consultationId = data!!.getInt(PushConstants.EXTRA_CONSULTATION_ID)
        try {
            // Create Notification Channel
            val channelId = NotificationChannelManager.createIfNeeded(this, CallChannel().asJSON(), false)
            CallChannel.CHANNEL_ID = channelId

            // Create Notification
            val notification = NotificationBuilder(this, channelId, consultationId)

            // Fallthrough IntentExtras
            data.putInt(PushConstants.EXTRA_NOTIFICATION_ID, notification.id)
            notification.setExtras(data)

            // Prepare notification for Call
            notification.prepareCall(
                data.getString(PushConstants.EXTRA_TITLE),
                data.getString(PushConstants.EXTRA_DESCRIPTION)
            )

            // Use notification payload param `color` for notification icon color
            val color = data.getString(PushConstants.EXTRA_COLOR)
            if (color != null && color.isNotEmpty()) {
                Debug("HeadsUpNotificationService", "onStartCommand", "Using payload color: $color")
                notification.setColor(color)
            }

            // Start showing the notification
            startForeground(notification.id, notification.build())

            // Mark call status as busy
            isBusy = true

            // Start ringing and vibrating
            ringtone!!.start(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE), true)

            // Wake Up device
            Tools.wakeUpDevice(this)

            // Create Lost Call after 20s
            // TODO: It should come from the server
            lostCallHandler = Handler(Looper.getMainLooper())
            lostCallHandler!!.postDelayed(
                {
                    try {
                        showLostCall(data)
                    } catch (e: CanceledException) {
                        e.printStackTrace()
                    }
                },
                20000
            )
        } catch (e: Exception) {
            clearCallNotification(this)
            e.printStackTrace()
        }
        return START_STICKY
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    @Throws(CanceledException::class)
    private fun showLostCall(extras: Bundle?) {
        val lostCallIntent =
            Intent(this, CallNotificationActionReceiver::class.java)
        lostCallIntent.putExtras(extras!!)
        lostCallIntent.action = "CANCEL_NOTIFICATION"
        val pi = PendingIntent.getBroadcast(
            this,
            0,
            lostCallIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        pi.send()
    }

    companion object {
        var TAG = PushPlugin.PREFIX_TAG + CallNotificationService::class.java
        var isBusy = false
        fun clearCallNotification(context: Context) {
            context.stopService(Intent(context, CallNotificationService::class.java))
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        fun createCallOnHold(context: Context, extras: Bundle) {
            // Create Notification Channel
            val channelId =
                NotificationChannelManager.createIfNeeded(context, MessageChannel().asJSON(), false)
            CallChannel.CHANNEL_ID = channelId

            // Create Notification
            val notification =
                NotificationBuilder(context, channelId, extras.getInt(PushConstants.EXTRA_CONSULTATION_ID))

            // Fallthrough IntentExtras
            extras.putInt(PushConstants.EXTRA_NOTIFICATION_ID, notification.id)
            notification.setExtras(extras)

            // Prepare notification for Call
            notification.prepareCallOnHold(extras.getString(PushConstants.EXTRA_DESCRIPTION))

            // Use notification payload param `color` for notification icon color
            val color = extras.getString(PushConstants.EXTRA_COLOR)
            if (color != null && color.isNotEmpty()) {
                Debug(
                    "HeadsUpNotificationService",
                    "createCallOnHold",
                    "Using payload color: $color"
                )
                notification.setColor(color)
            }

            // Show the notification
            notification.show()
        }
    }
}

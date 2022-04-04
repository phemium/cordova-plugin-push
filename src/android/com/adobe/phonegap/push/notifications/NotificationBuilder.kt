package com.adobe.phonegap.push.notifications

import android.R
import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.adobe.phonegap.push.PushConstants
import com.adobe.phonegap.push.PushPlugin
import com.adobe.phonegap.push.logs.Logger.Debug
import com.adobe.phonegap.push.logs.Logger.Error
import com.adobe.phonegap.push.utils.ServiceUtils
import java.util.concurrent.atomic.AtomicInteger

/**
 * Class used to create notifications
 */
class NotificationBuilder @RequiresApi(api = Build.VERSION_CODES.O) constructor(
    context: Context,
    channelId: String?,
    consultationId: Int
) {
    // Contains all extra parameters that will be passed in Actions
    private var extras = Bundle()

    // Notification Builder
    private val notificationBuilder: NotificationCompat.Builder =
        NotificationCompat.Builder(context, channelId!!)

    // Contains this notification ID
    private var notificationId = 0
    private val consultationId: Int
    val id: Int
        get() = notificationIds[consultationId]!!

    fun build(): Notification {
        return notificationBuilder.build()
    }

    private lateinit var context: Context

    fun getApplicationName(): String {
        val applicationInfo = context.applicationInfo
        val stringId = applicationInfo.labelRes
        return if (stringId == 0) applicationInfo.nonLocalizedLabel.toString() else context.getString(
            stringId
        )
    }

    fun setSmallIcon(icon: Int) {
        notificationBuilder.setSmallIcon(icon)
        Debug(TAG, "setSmallIcon", "OK")
    }

    fun setSmallIcon(iconName: String) {
        val iconId = context.resources.getIdentifier(
            iconName,
            "drawable",
            context.packageName
        )
        if (iconId != 0) {
            this.setSmallIcon(iconId)
        } else {
            Error(TAG, "setSmallIcon", "Unable to find icon with name $iconName")
        }
    }

    fun setContentClickHandler(pendingIntent: PendingIntent?) {
        if (pendingIntent != null) {
            notificationBuilder.setContentIntent(pendingIntent)
            Debug(TAG, "setContentIntent", "OK")
        } else {
            Error(TAG, "setContentIntent", "Passed PendingIntent is null")
        }
    }

    fun setFullScreenHandler(fullscreenIntent: PendingIntent?) {
        if (fullscreenIntent != null) {
            notificationBuilder.setFullScreenIntent(fullscreenIntent, true)
            Debug(TAG, "setFullScreenHandler", "OK")
        } else {
            Error(TAG, "setFullScreenHandler", "Passed PendingIntent is null")
        }
    }

    fun setSticky(ongoing: Boolean?) {
        notificationBuilder.setOngoing(ongoing!!)
        Debug(TAG, "setOngoing", "OK")
    }

    fun setTitle(title: String?) {
        notificationBuilder.setContentTitle(title)
        Debug(TAG, "setContentTitle", "OK")
    }

    private fun setHighPriority() {
        notificationBuilder.priority = NotificationCompat.PRIORITY_HIGH
        Debug(TAG, "setHighPriority", "OK")
    }

    fun setMaximumPriority() {
        notificationBuilder.priority = NotificationCompat.PRIORITY_MAX
        Debug(TAG, "setMaximumPriority", "OK")
    }

    fun setText(text: String?) {
        notificationBuilder.setContentText(text)
        Debug(TAG, "setContentText", "OK")
    }

    fun addActionButton(icon: Int, text: String?, handler: PendingIntent?) {
        notificationBuilder.addAction(icon, text, handler)
        Debug(TAG, "addAction", String.format("%s | %s", text, "OK"))
    }

    fun setCloseOnClick(cancellable: Boolean?) {
        notificationBuilder.setAutoCancel(cancellable!!)
        Debug(TAG, "setCancellable", "OK")
    }

    fun setCategory(category: String?) {
        notificationBuilder.setCategory(category)
        Debug(TAG, "setCategory", "OK")
    }

    fun setColor(color: Int) {
        notificationBuilder.color = color
        Debug(TAG, "setColor", "OK")
    }

    fun setColor(color: String?) {
        try {
            val parsedColor = Color.parseColor(color)
            this.setColor(parsedColor)
            Debug(TAG, "setColor", "OK")
        } catch (e: IllegalArgumentException) {
            Error(TAG, "setColor", "Passed Color is invalid")
        }
    }

    fun setVibrate(vibration: LongArray?) {
        notificationBuilder.setVibrate(vibration)
        Debug(TAG, "setColor", "OK")
    }

    fun setSound(sound: Uri?) {
        notificationBuilder.setSound(sound)
        Debug(TAG, "setSound", "OK")
    }

    fun setRingingSound() {
        notificationBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE))
        Debug(TAG, "setRingingSound", "OK")
    }

    fun setNotificationSound() {
        notificationBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
        Debug(TAG, "setNotificationSound", "OK")
    }

    fun setAlarmSound() {
        notificationBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
        Debug(TAG, "setAlarmSound", "OK")
    }

    private val cancelCallIntent: PendingIntent
        @SuppressLint("UnspecifiedImmutableFlag")
        get() {
            Debug(TAG, "getCancelCallIntent", "Started")
            val intent =
                Intent(context, CallNotificationActionReceiver::class.java)
            intent.putExtras(extras)
            intent.putExtra(
                PushConstants.CALL_RESPONSE_ACTION_KEY,
                PushConstants.CALL_CANCEL_ACTION
            )
            intent.action = PushConstants.CALL_CANCEL_ACTION
            val cancelIntent = PendingIntent.getBroadcast(
                context,
                1201,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            Debug(TAG, "getCancelCallIntent", "OK")
            return cancelIntent
        }
    private val fullScreenIntent: PendingIntent
        @SuppressLint("UnspecifiedImmutableFlag")
        get() {
            Debug(TAG, "getFullScreenIntent", "Started")
            val intent =
                Intent(context, CallNotificationActionReceiver::class.java)
            intent.putExtras(extras)
            intent.putExtra(PushConstants.CALL_RESPONSE_ACTION_KEY, PushConstants.VIEW_CALL_ACTION)
            intent.action = PushConstants.VIEW_CALL_ACTION
            val cancelIntent = PendingIntent.getBroadcast(
                context,
                1205,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            Debug(TAG, "getFullScreenIntent", "OK")
            return cancelIntent
        }
    private val acceptCallIntent: PendingIntent
        @SuppressLint("UnspecifiedImmutableFlag")
        get() {
            Debug(TAG, "getAcceptCallIntent", "Started")
            val intent =
                Intent(context, CallNotificationActionReceiver::class.java)
            intent.putExtras(extras)
            intent.putExtra(
                PushConstants.CALL_RESPONSE_ACTION_KEY,
                PushConstants.CALL_ACCEPT_ACTION
            )
            intent.action = PushConstants.CALL_ACCEPT_ACTION
            val acceptIntent = PendingIntent.getBroadcast(
                context,
                1200,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            Debug(TAG, "getAcceptCallIntent", "Started")
            return acceptIntent
        }

    fun setExtras(data: Bundle) {
        extras = data
    }

    fun show() {
        ServiceUtils.notificationService(context)
            .notify(this.getApplicationName(), notificationId, notificationBuilder.build())
    }

    /**
     * This method allows to automatically build a notification with parameters of a call
     * @param from Name of person who is calling me
     * @param text Text to be put under the name as a description
     * @return void
     */
    fun prepareCall(
        from: String?,
        text: String?
    ) {
        Debug(TAG, "prepareCall", "Started")
        val fullScreenCallHandler = fullScreenIntent
        setCategory(NotificationCompat.CATEGORY_CALL)
        setTitle(from)
        setText(text)
        setCloseOnClick(false)
        setMaximumPriority()
        setSticky(true)
        this.setSmallIcon(R.drawable.sym_call_incoming)
        setContentClickHandler(fullScreenCallHandler)
        addActionButton(R.drawable.ic_menu_call, "Rechazar", cancelCallIntent)
        addActionButton(R.drawable.ic_menu_call, "Aceptar", acceptCallIntent)
        setFullScreenHandler(fullScreenCallHandler)
        Debug(TAG, "prepareCall", "OK")
    }

    /**
     * This method allows to automatically build a notification with parameters of a call on hold
     * @param text Text to be put under the name as a description
     * @return void
     */
    fun prepareCallOnHold(
        text: String?
    ) {
        Debug(TAG, "prepareCallOnHold", "Started")
        val viewCallHandler = fullScreenIntent
        setCategory(NotificationCompat.CATEGORY_CALL)
        setTitle("Llamada en espera")
        setText(text)
        setCloseOnClick(true)
        setHighPriority()
        setSticky(false)
        this.setSmallIcon(R.drawable.sym_call_incoming)
        setContentClickHandler(viewCallHandler)
        addActionButton(R.drawable.ic_menu_call, "Ir a la consulta", viewCallHandler)
        Debug(TAG, "prepareCallOnHold", "OK")
    }

    fun applicationName(): String {
            val applicationInfo = context.applicationInfo
            val stringId = applicationInfo.labelRes
            return if (stringId == 0) applicationInfo.nonLocalizedLabel.toString() else context.getString(
                stringId
            )
        }

    companion object {
        private const val TAG = PushPlugin.PREFIX_TAG + " (NotificationBuilder)"

        // Variable holding the ID for current and future notifications
        private val c = AtomicInteger(100)
        var notificationIds = HashMap<Int, Int>()
    }

    fun clearAll() {
        ServiceUtils.notificationService(context).cancelAll()
    }

    /** Constructor  */
    init {
        notificationBuilder.setDefaults(Notification.DEFAULT_ALL)
        this.consultationId = consultationId
        if (notificationIds.containsKey(consultationId)) {
            notificationId = notificationIds[consultationId]!!
        } else {
            notificationId = c.incrementAndGet()
            notificationIds[consultationId] = notificationId
        }
        Debug(TAG, "NotificationID", notificationId)
        this.context = context
    }
}
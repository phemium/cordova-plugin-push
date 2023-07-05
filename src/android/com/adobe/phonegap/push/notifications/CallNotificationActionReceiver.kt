package com.adobe.phonegap.push.notifications

import com.adobe.phonegap.push.logs.Logger.Debug
import com.adobe.phonegap.push.PushPlugin.Companion.createLostCall
import android.content.BroadcastReceiver
import com.adobe.phonegap.push.PushPlugin
import androidx.annotation.RequiresApi
import android.os.Build
import android.content.Intent
import android.os.Bundle
import com.adobe.phonegap.push.PushConstants
import android.content.Context
import com.adobe.phonegap.push.utils.ServiceUtils
// BEGIN PEM
import com.phemium.plugins.PhemiumEnduserActivity
// END PEM

class CallNotificationActionReceiver : BroadcastReceiver() {
    private val TAG = PushPlugin.PREFIX_TAG + " (Receiver)"
    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        Debug(TAG, "onReceive", "Received action from Call Notification")
        val action = intent.action
        val extras = intent.extras
        if (action == PushConstants.CALL_CANCEL_ACTION || action == PushConstants.VIEW_CALL_ACTION || action == PushConstants.CALL_ACCEPT_ACTION || action == PushConstants.VIEW_CHAT_ACTION) {
            performClickAction(context, action, extras)

            // Close the notification after the click action is performed.
            val closeDialogs = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
            context.sendBroadcast(closeDialogs)
        } else if (action == "CANCEL_NOTIFICATION") {
            // Cancel notification
            val notificationManager = ServiceUtils.notificationService(context)
            notificationManager.cancel(extras!!.getInt(PushConstants.EXTRA_NOTIFICATION_ID))
            Debug(TAG, "onReceive", "Cancelling call")
            CallNotificationService.clearCallNotification(context)
            createLostCall(context, extras)
        }
    }

    fun performClickAction(context: Context, action: String, extras: Bundle?) {
        if (action === PushConstants.CALL_ACCEPT_ACTION) {
            Debug(TAG, "performClickAction", "Starting activity PEM for CALL_ACCEPT_ACTION action")
            openEnduserCall(context, extras, true)
        } else if (action === PushConstants.VIEW_CALL_ACTION) {
            Debug(TAG, "performClickAction", "Starting activity PEM for VIEW_CALL action")
            openEnduserCall(context, extras, null)
        } else if (action === PushConstants.CALL_CANCEL_ACTION) {

            // TODO: We should send something to the server so it knows it has been rejected
            Debug(TAG, "performClickAction", "CALL_CANCEL_ACTION action")
            CallNotificationService.clearCallNotification(context)
        } else if (action === PushConstants.VIEW_CHAT_ACTION) {
            Debug(TAG, "performClickAction", "Starting activity PEM for VIEW_CHAT_ACTION action")
            openEnduser(context, extras)
        }
    }

    /**
     * Opens PEM Chat
     * @param context
     */
    private fun openEnduser(context: Context, extras: Bundle?) {
        // BEGIN PEM
        val intent = Intent(context, PhemiumEnduserActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra(
            "url_params",
            PEM.getEnduserParamsForChat(extras!!.getInt(PushConstants.EXTRA_CONSULTATION_ID))
        )
        context.startActivity(intent)
        // END PEM
    }

    /**
     * Opens PEM with Call dialog
     * @param context
     * @param acceptCall:
     * - null  -> View call dialog
     * - true  -> Accept call
     * - false -> Deny call
     */
    private fun openEnduserCall(context: Context, extras: Bundle?, acceptCall: Boolean?) {
        // BEGIN PEM
        val intent = Intent(context, PhemiumEnduserActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra(
            "url_params",
            PEM.getEnduserParamsForCall(
                extras!!.getInt(PushConstants.EXTRA_CONSULTATION_ID),
                acceptCall
            )
        )
        context.startActivity(intent)
        // END PEM
    }
}
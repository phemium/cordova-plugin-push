package com.adobe.phonegap.push.notifications;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;

import com.adobe.phonegap.push.PushConstants;
import com.adobe.phonegap.push.PushPlugin;
import com.adobe.phonegap.push.logs.Logger;
import com.adobe.phonegap.push.utils.ServiceUtils;
import com.phemium.plugins.PhemiumEnduserActivity;

public class CallNotificationActionReceiver extends BroadcastReceiver {
    private final String TAG = PushPlugin.PREFIX_TAG + " (Receiver)";

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.Debug(TAG, "onReceive", "Received action from Call Notification");
        final String action = intent.getAction();
        Bundle extras = intent.getExtras();
        if (
                action.equals(PushConstants.CALL_CANCEL_ACTION) ||
                action.equals(PushConstants.VIEW_CALL_ACTION) ||
                action.equals(PushConstants.CALL_ACCEPT_ACTION) ||
                action.equals(PushConstants.VIEW_CHAT_ACTION)
        ) {
            performClickAction(context, action, extras);

            // Close the notification after the click action is performed.
            Intent closeDialogs = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            context.sendBroadcast(closeDialogs);
        } else if (action.equals("CANCEL_NOTIFICATION")) {
            // Cancel notification
            NotificationManager notificationManager = ServiceUtils.getNotificationService();
            notificationManager.cancel(extras.getInt(PushConstants.EXTRA_NOTIFICATION_ID));
            Logger.Debug(TAG, "onReceive", "Cancelling call");
            CallNotificationService.clearCallNotification(context);
            PushPlugin.createLostCall(extras);
        }
    }

    public void performClickAction(Context context, String action, Bundle extras) {

        if (action == PushConstants.CALL_ACCEPT_ACTION) {

            Logger.Debug(TAG, "performClickAction", "Starting activity PEM for CALL_ACCEPT_ACTION action");

            openEnduserCall(context, extras, null);

        } else if (action == PushConstants.VIEW_CALL_ACTION) {

            Logger.Debug(TAG, "performClickAction", "Starting activity PEM for VIEW_CALL action");

            openEnduserCall(context, extras, null);

        } else if (action == PushConstants.CALL_CANCEL_ACTION) {

            // TODO: We should send something to the server so it knows it has been rejected
            Logger.Debug(TAG, "performClickAction", "CALL_CANCEL_ACTION action");

            CallNotificationService.clearCallNotification(context);

        } else if (action == PushConstants.VIEW_CHAT_ACTION) {

            Logger.Debug(TAG, "performClickAction", "Starting activity PEM for VIEW_CHAT_ACTION action");

            openEnduser(context, extras);
        }

    }


    /**
     * Opens PEM Chat
     * @param context
     */
    private void openEnduser(Context context, Bundle extras) {
        Intent intent = new Intent(context, PhemiumEnduserActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("url_params", PEM.getEnduserParamsForChat(extras.getInt(PushConstants.EXTRA_CONSULTATION_ID)));
        context.startActivity(intent);
    }

    /**
     * Opens PEM with Call dialog
     * @param context
     * @param acceptCall:
     *        - null  -> View call dialog
     *        - true  -> Accept call
     *        - false -> Deny call
     */
    private void openEnduserCall(Context context, Bundle extras, Boolean acceptCall) {
        Intent intent = new Intent(context, PhemiumEnduserActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("url_params", PEM.getEnduserParamsForCall(extras.getInt(PushConstants.EXTRA_CONSULTATION_ID), acceptCall));
        context.startActivity(intent);
    }
}
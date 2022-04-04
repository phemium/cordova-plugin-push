package com.adobe.phonegap.push.notifications;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.RequiresApi;

import com.adobe.phonegap.push.PushConstants;
import com.adobe.phonegap.push.PushPlugin;
import com.adobe.phonegap.push.logs.Logger;
import com.adobe.phonegap.push.notifications.channels.CallChannel;
import com.adobe.phonegap.push.notifications.channels.MessageChannel;
import com.adobe.phonegap.push.utils.Globals;
import com.adobe.phonegap.push.utils.Tools;


public class CallNotificationService extends Service {

    public static String TAG = PushPlugin.PREFIX_TAG + CallNotificationService.class;

    public static Boolean isBusy = false;
    private Handler lostCallHandler;
    private IncomingRinger ringtone;

    @Override
     public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        if (this.ringtone == null) {
            this.ringtone = new IncomingRinger(Globals.applicationContext);
        }
    }

    @Override
    public void onDestroy() {
        isBusy = false;
        if (ringtone != null) {
            ringtone.stop();
        }
        if (lostCallHandler != null) {
            lostCallHandler.removeCallbacksAndMessages(null);
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Retrieve IntentExtras
        Bundle data = intent.getExtras();

        // Get consultation ID
        int consultationId = data.getInt(PushConstants.EXTRA_CONSULTATION_ID);

        try {
            // Create Notification Channel
            String channelId = NotificationChannelManager.createIfNeeded(new CallChannel().asJSON(), false);
            CallChannel.CHANNEL_ID = channelId;

            // Create Notification
            NotificationBuilder notification = new NotificationBuilder(channelId, consultationId);

            // Passthrough IntentExtras
            data.putInt(PushConstants.EXTRA_NOTIFICATION_ID, notification.getId());
            notification.setExtras(data);

            // Prepare notification for Call
            notification.prepareCall( data.getString(PushConstants.EXTRA_TITLE), data.getString(PushConstants.EXTRA_DESCRIPTION) );

            // Use notification payload param `color` for notification icon color
            String color = data.getString(PushConstants.EXTRA_COLOR);
            if (color != null && !color.isEmpty()) {
                Logger.Debug("HeadsUpNotificationService", "onStartCommand", "Using payload color: " + color);
                notification.setColor(color);
            }

            // Start showing the notification
            startForeground(notification.getId(), notification.build());

            // Mark call status as busy
            isBusy = true;

            // Start ringing and vibrating
            this.ringtone.start(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE), true);

            // Wake Up device
            Tools.wakeUpDevice();

            // Create Lost Call after 20s
            // TODO: It should come from the server
            lostCallHandler = new Handler(Looper.getMainLooper());
            lostCallHandler.postDelayed(
                    () -> {
                        try {
                            showLostCall(data);
                        } catch (PendingIntent.CanceledException e) {
                            e.printStackTrace();
                        }
                    },
                    20000);
        } catch (Exception e) {
            clearCallNotification(this);
            e.printStackTrace();
        }
        return START_STICKY;
    }

    public static void clearCallNotification(Context context) {
        context.stopService(new Intent(context, CallNotificationService.class));
    }

    private void showLostCall(Bundle extras) throws PendingIntent.CanceledException {
        Intent lostCallIntent = new Intent(Globals.applicationContext, CallNotificationActionReceiver.class);
        lostCallIntent.putExtras(extras);
        lostCallIntent.setAction("CANCEL_NOTIFICATION");
        PendingIntent pi = PendingIntent.getBroadcast(Globals.applicationContext, 0, lostCallIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        pi.send();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void createCallOnHold(Bundle extras) {
        // Create Notification Channel
        String channelId = NotificationChannelManager.createIfNeeded(new MessageChannel().asJSON(), false);
        CallChannel.CHANNEL_ID = channelId;

        // Create Notification
        NotificationBuilder notification = new NotificationBuilder(channelId, extras.getInt(PushConstants.EXTRA_CONSULTATION_ID));

        // Passthrough IntentExtras
        extras.putInt(PushConstants.EXTRA_NOTIFICATION_ID, notification.getId());
        notification.setExtras(extras);

        // Prepare notification for Call
        notification.prepareCallOnHold( extras.getString(PushConstants.EXTRA_DESCRIPTION) );

        // Use notification payload param `color` for notification icon color
        String color = extras.getString(PushConstants.EXTRA_COLOR);
        if (color != null && !color.isEmpty()) {
            Logger.Debug("HeadsUpNotificationService", "createCallOnHold", "Using payload color: " + color);
            notification.setColor(color);
        }

        // Show the notification
        notification.show();
    }
}
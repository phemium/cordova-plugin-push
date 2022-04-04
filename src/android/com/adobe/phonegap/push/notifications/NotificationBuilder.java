package com.adobe.phonegap.push.notifications;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.R;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.adobe.phonegap.push.PushConstants;
import com.adobe.phonegap.push.PushPlugin;
import com.adobe.phonegap.push.logs.Logger;
import com.adobe.phonegap.push.utils.Globals;
import com.adobe.phonegap.push.utils.ServiceUtils;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class used to create notifications
 */
public class NotificationBuilder {

    private static String TAG = PushPlugin.PREFIX_TAG + " (NotificationBuilder)";

    // Contains all extra parameters that will be passed in Actions
    private Bundle extras = new Bundle();
    // Notification Builder
    private final NotificationCompat.Builder notificationBuilder;
    // Variable holding the ID for current and future notifications
    private final static AtomicInteger c = new AtomicInteger(100);
    // Contains this notification ID
    private final int notificationId;
    private final int consultationId;

    public int getId() {
        return notificationIds.get(this.consultationId);
    }

    public static HashMap<Integer, Integer> notificationIds = new HashMap<>();

    /** Constructor */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public NotificationBuilder(String channelId, int consultationId) {
        this.notificationBuilder = new NotificationCompat.Builder(Globals.applicationContext, channelId);
        this.notificationBuilder.setDefaults(Notification.DEFAULT_ALL);
        this.consultationId = consultationId;
        if (notificationIds.containsKey(consultationId)) {
            this.notificationId = notificationIds.get(consultationId);
        } else {
            this.notificationId = this.c.incrementAndGet();
            notificationIds.put(consultationId, this.notificationId);
        }
        Logger.Debug(TAG, "NotificationID", this.notificationId);
    }

    public Notification build() {
        return this.notificationBuilder.build();
    }

    public void setSmallIcon(int icon) {
        this.notificationBuilder.setSmallIcon(icon);
        Logger.Debug(TAG, "setSmallIcon", "OK");
    }

    public void setSmallIcon(String iconName) {
        int iconId = Globals.applicationContext.getResources().getIdentifier(iconName, "drawable", Globals.applicationContext.getPackageName());
        if (iconId != 0) {
            this.setSmallIcon(iconId);
        } else {
            Logger.Error(TAG, "setSmallIcon", "Unable to find icon with name " + iconName);
        }
    }

    public void setContentClickHandler(PendingIntent pendingIntent) {
        if (pendingIntent != null) {
            this.notificationBuilder.setContentIntent(pendingIntent);
            Logger.Debug(TAG, "setContentIntent", "OK");
        } else {
            Logger.Error(TAG, "setContentIntent", "Passed PendingIntent is null");
        }
    }

    public void setFullScreenHandler(PendingIntent fullscreenIntent) {
        if (fullscreenIntent != null) {
            this.notificationBuilder.setFullScreenIntent(fullscreenIntent, true);
            Logger.Debug(TAG, "setFullScreenHandler", "OK");
        } else {
            Logger.Error(TAG, "setFullScreenHandler", "Passed PendingIntent is null");
        }
    }

    public void setSticky(Boolean ongoing) {
        this.notificationBuilder.setOngoing(ongoing);
        Logger.Debug(TAG, "setOngoing", "OK");
    }

    public void setTitle(String title) {
        this.notificationBuilder.setContentTitle(title);
        Logger.Debug(TAG, "setContentTitle", "OK");
    }

    public void setHighPriority() {
        this.notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        Logger.Debug(TAG, "setHighPriority", "OK");
    }

    public void setMaximumPriority() {
        this.notificationBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
        Logger.Debug(TAG, "setMaximumPriority", "OK");
    }

    public void setText(String text) {
        this.notificationBuilder.setContentText(text);
        Logger.Debug(TAG, "setContentText", "OK");
    }

    public void addActionButton(int icon, String text, PendingIntent handler) {
        this.notificationBuilder.addAction(icon, text, handler);
        Logger.Debug(TAG, "addAction", String.format("%s | %s", text, "OK"));
    }

    public void setCloseOnClick(Boolean cancellable) {
        this.notificationBuilder.setAutoCancel(cancellable);
        Logger.Debug(TAG, "setCancellable", "OK");
    }

    public void setCategory(String category) {
        this.notificationBuilder.setCategory(category);
        Logger.Debug(TAG, "setCategory", "OK");
    }

    public void setColor(int color) {
        this.notificationBuilder.setColor(color);
        Logger.Debug(TAG, "setColor", "OK");
    }

    public void setColor(String color) {
        try {
            int parsedColor = Color.parseColor(color);
            this.setColor(parsedColor);
            Logger.Debug(TAG, "setColor", "OK");
        } catch (IllegalArgumentException e) {
            Logger.Error(TAG, "setColor", "Passed Color is invalid");
        }
    }

    public void setVibrate(long[] vibration) {
        this.notificationBuilder.setVibrate(vibration);
        Logger.Debug(TAG, "setColor", "OK");
    }

    public void setSound(Uri sound) {
        this.notificationBuilder.setSound(sound);
        Logger.Debug(TAG, "setSound", "OK");
    }

    public void setRingingSound() {
        this.notificationBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE));
        Logger.Debug(TAG, "setRingingSound", "OK");
    }

    public void setNotificationSound() {
        this.notificationBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        Logger.Debug(TAG, "setNotificationSound", "OK");
    }

    public void setAlarmSound() {
        this.notificationBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
        Logger.Debug(TAG, "setAlarmSound", "OK");
    }

    private PendingIntent getCancelCallIntent() {
        Logger.Debug(TAG, "getCancelCallIntent", "Started");
        Intent intent = new Intent(Globals.applicationContext, CallNotificationActionReceiver.class);
        intent.putExtras(this.extras);
        intent.putExtra( PushConstants.CALL_RESPONSE_ACTION_KEY, PushConstants.CALL_CANCEL_ACTION );
        intent.setAction(PushConstants.CALL_CANCEL_ACTION);
        PendingIntent cancelIntent = PendingIntent.getBroadcast(
                Globals.applicationContext,
                1201,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        Logger.Debug(TAG, "getCancelCallIntent", "OK");
        return cancelIntent;
    }

    private PendingIntent getFullScreenIntent() {
        Logger.Debug(TAG, "getFullScreenIntent", "Started");
        Intent intent = new Intent(Globals.applicationContext, CallNotificationActionReceiver.class);
        intent.putExtras(this.extras);
        intent.putExtra( PushConstants.CALL_RESPONSE_ACTION_KEY, PushConstants.VIEW_CALL_ACTION );
        intent.setAction(PushConstants.VIEW_CALL_ACTION);
        PendingIntent cancelIntent = PendingIntent.getBroadcast(Globals.applicationContext, 1205, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Logger.Debug(TAG, "getFullScreenIntent", "OK");
        return cancelIntent;
    }

    private PendingIntent getAcceptCallIntent() {
        Logger.Debug(TAG, "getAcceptCallIntent", "Started");
        Intent intent = new Intent(Globals.applicationContext, CallNotificationActionReceiver.class);
        intent.putExtras(this.extras);
        intent.putExtra( PushConstants.CALL_RESPONSE_ACTION_KEY, PushConstants.CALL_ACCEPT_ACTION );
        intent.setAction(PushConstants.CALL_ACCEPT_ACTION);
        PendingIntent acceptIntent = PendingIntent.getBroadcast(
                Globals.applicationContext,
                1200,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        Logger.Debug(TAG, "getAcceptCallIntent", "Started");
        return acceptIntent;
    }

    public static String getApplicationName() {
        ApplicationInfo applicationInfo = Globals.applicationContext.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : Globals.applicationContext.getString(stringId);
    }

    public void setExtras(Bundle data) {
        this.extras = data;
    }

    public void show() {
        ServiceUtils.getNotificationService().notify(this.getApplicationName(), this.notificationId, this.notificationBuilder.build());
    }

    /**
     * This method allows to automatically build a notification with parameters of a call
     * @param from Name of person who is calling me
     * @param text Text to be put under the name as a description
     * @return void
     */
    public void prepareCall(
            String from,
            String text
    ) {
        Logger.Debug(TAG, "prepareCall", "Started");
        PendingIntent fullScreenCallHandler = this.getFullScreenIntent();
        this.setCategory(NotificationCompat.CATEGORY_CALL);
        this.setTitle(from);
        this.setText(text);
        this.setCloseOnClick(false);
        this.setMaximumPriority();
        this.setSticky(true);
        this.setSmallIcon(R.drawable.sym_call_incoming);
        this.setContentClickHandler(fullScreenCallHandler);
        this.addActionButton(R.drawable.ic_menu_call, "Rechazar", this.getCancelCallIntent());
        this.addActionButton(R.drawable.ic_menu_call, "Aceptar", this.getAcceptCallIntent());
        this.setFullScreenHandler(fullScreenCallHandler);
        Logger.Debug(TAG, "prepareCall", "OK");
    }

    /**
     * This method allows to automatically build a notification with parameters of a call on hold
     * @param text Text to be put under the name as a description
     * @return void
     */
    public void prepareCallOnHold(
            String text
    ) {
        Logger.Debug(TAG, "prepareCallOnHold", "Started");
        PendingIntent viewCallHandler = this.getFullScreenIntent();
        this.setCategory(NotificationCompat.CATEGORY_CALL);
        this.setTitle("Llamada en espera");
        this.setText(text);
        this.setCloseOnClick(true);
        this.setHighPriority();
        this.setSticky(false);
        this.setSmallIcon(R.drawable.sym_call_incoming);
        this.setContentClickHandler(viewCallHandler);
        this.addActionButton(R.drawable.ic_menu_call, "Ir a la consulta", viewCallHandler);
        Logger.Debug(TAG, "prepareCallOnHold", "OK");
    }

    public static void clearAll() {
        ServiceUtils.getNotificationService().cancelAll();
    }

    public static void clearOne(int id) {
        ServiceUtils.getNotificationService().cancel(getApplicationName(), id);
    }

}

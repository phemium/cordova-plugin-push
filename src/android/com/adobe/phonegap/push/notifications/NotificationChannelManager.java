package com.adobe.phonegap.push.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;

import com.adobe.phonegap.push.PushPlugin;
import com.adobe.phonegap.push.logs.Logger;
import com.adobe.phonegap.push.utils.Globals;
import com.adobe.phonegap.push.utils.ServiceUtils;

import java.util.List;
import java.util.Date;

public class NotificationChannelManager {

    private static String TAG = PushPlugin.PREFIX_TAG + NotificationChannelManager.class;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static List<NotificationChannel> getList() {
        return ServiceUtils.getNotificationService().getNotificationChannels();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static NotificationChannel getOne(String channelId) {
        NotificationManager notificationManager = ServiceUtils.getNotificationService();
        return notificationManager.getNotificationChannel(channelId);
    }

    /**
     * Automatically creates a Notification Channel with the given specs
     * @param force
     */
    public static String createIfNeeded(Bundle params, Boolean force) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Logger.Debug(TAG, "createIfNeeded", "Channel: " + params.getString("id"));
            NotificationManager notificationManager = ServiceUtils.getNotificationService();
            List<NotificationChannel> notificationChannels = notificationManager.getNotificationChannels();
            String foundId = null;
            for (NotificationChannel notificationChannel : notificationChannels) {
                String channelId = notificationChannel.getId();
                if (channelId.startsWith(params.getString("id"))) {
                    foundId = channelId;
                    if (force) {
                        foundId = null;
                        delete(channelId);
                    }
                }
            }
            if (foundId != null) {
                Logger.setDebug(true);
                Logger.Debug(TAG, "createIfNeeded", "Channel is found and not forced to be recreated: " + foundId);
                return foundId;
            } else {
                Logger.setDebug(true);
                Logger.Debug(TAG, "createIfNeeded", "Creating channel as new...");
                String newChannelId = params.getString("id") + (new Date().getTime());
                Logger.Debug(TAG, "createIfNeeded", "ChannelID: " + newChannelId);
                // Set ID, Name and Importance
                NotificationChannel notificationchannel = new NotificationChannel(
                        newChannelId,
                        params.getString("name"),
                        params.getInt("priority")
                );
                // Set Visibility
                notificationchannel.setLockscreenVisibility(params.getInt("visibility"));
                Logger.Debug(TAG, "createIfNeeded", "visibility: " + params.getInt("visibility"));
                // Set Call Sound or not
                Uri sound = params.getParcelable("sound");
                AudioAttributes audioAttributes = params.getParcelable("audioAttributes");
                if (sound != null && audioAttributes != null) {
                    Logger.Debug(TAG, "createIfNeeded", "Setting sound");
                    notificationchannel.setSound(sound, audioAttributes);
                }
                // Set Vibration
                if (params.getBoolean("vibration")) {
                    Logger.Debug(TAG, "createIfNeeded", "vibration: " + params.getLongArray("vibrationPattern"));
                    notificationchannel.setVibrationPattern(params.getLongArray("vibrationPattern"));
                    notificationchannel.enableVibration(true);
                }
                // Set Lights
                int light = params.getInt("light");
                if (light != -1) {
                    Logger.Debug(TAG, "createIfNeeded", "light: " + light);
                    notificationchannel.setLightColor(light);
                    notificationchannel.enableLights(true);
                }
                // Set Show Badge
                Boolean showBadge = params.getBoolean("showBadge");
                notificationchannel.setShowBadge(showBadge);
                // Set priority
                notificationchannel.setImportance(NotificationManager.IMPORTANCE_HIGH);
                // Create channel
                Logger.Debug(TAG, "createIfNeeded", "showBadge: " + showBadge);
                notificationManager.createNotificationChannel(notificationchannel);
                return newChannelId;
            }
        } else {
            return null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void delete(String channelId) {
        NotificationManager mNotificationManager = ServiceUtils.getNotificationService();
        if (mNotificationManager != null) {
            mNotificationManager.deleteNotificationChannel(channelId);
        } else {
            Logger.Error(TAG, "delete("+channelId+")", "failed");
        }
    }
}

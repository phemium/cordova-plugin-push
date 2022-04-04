package com.adobe.phonegap.push.notifications.channels;

import android.app.Notification;
import android.app.NotificationManager;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;

import com.adobe.phonegap.push.utils.Globals;

public class MessageChannel implements ICustomChannel {
    public static String CHANNEL_ID = "";
    public static final String PREFIX_ID = "Messages";
    public static String name = "Messages";
    public static final Boolean vibration = true;
    public static final long[] vibrationPattern = {0, 500, 1000};
    public static final int visibility = Notification.VISIBILITY_PUBLIC;
    public static final Uri sound = null;
    public static final AudioAttributes audioAttributes = new AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build();
    public static final int light = Color.BLUE;
    public static final Boolean showBadge = true;
    public static final int priority = NotificationManager.IMPORTANCE_HIGH;

    public MessageChannel() {
        if (Globals.language == "es") {
            name = "Mensajes";
        } else if (Globals.language == "en") {
            name = "Messages";
        } else if (Globals.language == "ca") {
            name = "Missatges";
        } else if (Globals.language == "pt") {
            name = "Mensagens";
        }
    }

    public Bundle asJSON() {
        Bundle json = new Bundle();
        json.putString("id", PREFIX_ID);
        json.putString("name", name);
        json.putBoolean("vibration", vibration);
        json.putBoolean("showBadge", showBadge);
        json.putLongArray("vibrationPattern", vibrationPattern);
        json.putInt("visibility", visibility);
        json.putInt("light", light);
        json.putInt("priority", priority);
        json.putParcelable("sound", sound);
        json.putParcelable("audioAttributes", audioAttributes);
        return json;
    }
}

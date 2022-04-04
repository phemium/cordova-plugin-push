package com.adobe.phonegap.push.notifications.channels;

import android.app.Notification;
import android.app.NotificationManager;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;

import com.adobe.phonegap.push.utils.Globals;

public class CallChannel implements ICustomChannel {
    public static String CHANNEL_ID = "";
    public static final String PREFIX_ID = "VoIP";
    public static String name = "Calls";
    public static final Boolean vibration = true;
    public static final long[] vibrationPattern = {0, 500, 1000};
    public static final int visibility = Notification.VISIBILITY_PUBLIC;
    public static final int light = Color.GREEN;
    public static final Boolean showBadge = true;
    public static final int priority = NotificationManager.IMPORTANCE_MAX;

    public CallChannel() {
        if (Globals.language == "es") {
            name = "Llamadas";
        } else if (Globals.language == "en") {
            name = "Calls";
        } else if (Globals.language == "ca") {
            name = "Trucades";
        } else if (Globals.language == "pt") {
            name = "Chamadas";
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
        return json;
    }
}

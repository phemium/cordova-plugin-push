package com.adobe.phonegap.push.notifications.channels;

import android.app.Notification;
import android.app.NotificationManager;
import android.media.AudioAttributes;
import android.net.Uri;
import android.provider.Settings;

public interface ICustomChannel {
    String CHANNEL_ID = "";
    String PREFIX_ID = "";
    String name = "";
    Boolean vibration = false;
    long[] vibrationPattern = {};
    int visibility = Notification.VISIBILITY_PUBLIC;
    Uri sound = Settings.System.DEFAULT_RINGTONE_URI;
    AudioAttributes audioAttributes = null;
    int light = -1;
    Boolean showBadge = false;
    int priority = NotificationManager.IMPORTANCE_DEFAULT;
}

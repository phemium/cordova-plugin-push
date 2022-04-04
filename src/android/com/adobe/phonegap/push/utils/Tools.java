package com.adobe.phonegap.push.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.PowerManager;

import com.adobe.phonegap.push.PushConstants;

import kotlin.jvm.JvmStatic;
import me.leolin.shortcutbadger.ShortcutBadger;

public class Tools {

    /**
     * Retrieves the badge count from SharedPreferences
     *
     * @return Int
     */
    public static int getApplicationIconBadgeNumber() {
        SharedPreferences settings = Globals.applicationContext.getSharedPreferences(PushConstants.BADGE, Context.MODE_PRIVATE);
        return settings.getInt(PushConstants.BADGE, 0);
    }

    /**
     * Sets badge count on application icon and in SharedPreferences
     *
     * @param badgeCount
     */
    @JvmStatic
    public static void setApplicationIconBadgeNumber(int badgeCount) {
        if (badgeCount > 0) {
            ShortcutBadger.applyCount(Globals.applicationContext, badgeCount);
        } else {
            ShortcutBadger.removeCount(Globals.applicationContext);
        }

        SharedPreferences settings = Globals.applicationContext.getSharedPreferences(PushConstants.BADGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = settings.edit();
        prefEditor.putInt(PushConstants.BADGE, Tools.coerceAtLeast(badgeCount,0));
        prefEditor.apply();
    }

    private static int coerceAtLeast(int number, int min) {
        if (number < min) {
            return min;
        }
        return number;
    }

    public static void wakeUpDevice() {
        PowerManager mPowerManager = ServiceUtils.getPowerService();
        PowerManager.WakeLock wl = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "pushplugin:wakelock");
        wl.acquire();
    }

}

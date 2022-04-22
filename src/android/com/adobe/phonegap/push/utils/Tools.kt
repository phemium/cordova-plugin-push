package com.adobe.phonegap.push.utils

import android.content.Context
import com.adobe.phonegap.push.utils.ServiceUtils.powerService
import com.adobe.phonegap.push.PushConstants
import me.leolin.shortcutbadger.ShortcutBadger
import android.os.PowerManager

object Tools {
    fun getApplicationIconBadgeNumber(context: Context): Int {
        val settings = context.getSharedPreferences(
            PushConstants.BADGE,
            Context.MODE_PRIVATE
        )
        return settings.getInt(PushConstants.BADGE, 0)
    }

    fun setApplicationIconBadgeNumber(context: Context, badgeCount: Int) {
        if (badgeCount > 0) {
            ShortcutBadger.applyCount(context, badgeCount)
        } else {
            ShortcutBadger.removeCount(context)
        }
        val settings = context.getSharedPreferences(
            PushConstants.BADGE,
            Context.MODE_PRIVATE
        )
        val prefEditor = settings.edit()
        prefEditor.putInt(PushConstants.BADGE, coerceAtLeast(badgeCount, 0))
        prefEditor.apply()
    }

    private fun coerceAtLeast(number: Int, min: Int): Int {
        return if (number < min) {
            min
        } else number
    }

    fun wakeUpDevice(context: Context) {
        val mPowerManager = powerService(context)
        val wl = mPowerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "pushplugin:wakelock"
        )
        wl.acquire(10*60*1000L /*10 minutes*/)
    }
}
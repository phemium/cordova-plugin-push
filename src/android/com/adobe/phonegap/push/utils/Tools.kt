package com.adobe.phonegap.push.utils

import android.content.Context
import com.adobe.phonegap.push.utils.ServiceUtils.powerService
import com.adobe.phonegap.push.PushConstants
import kotlin.jvm.JvmStatic
import me.leolin.shortcutbadger.ShortcutBadger
import android.os.PowerManager

object Tools {
    /**
     * Retrieves the badge count from SharedPreferences
     *
     * @return Int
     */
    /**
     * Sets badge count on application icon and in SharedPreferences
     *
     * @param badgeCount
     */
    @set:JvmStatic
    var applicationIconBadgeNumber: Int
        get() {
            val settings = Globals.applicationContext!!.getSharedPreferences(
                PushConstants.BADGE,
                Context.MODE_PRIVATE
            )
            return settings.getInt(PushConstants.BADGE, 0)
        }
        set(badgeCount) {
            if (badgeCount > 0) {
                ShortcutBadger.applyCount(Globals.applicationContext, badgeCount)
            } else {
                ShortcutBadger.removeCount(Globals.applicationContext)
            }
            val settings = Globals.applicationContext!!.getSharedPreferences(
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

    fun wakeUpDevice() {
        val mPowerManager = powerService
        val wl = mPowerManager.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "pushplugin:wakelock"
        )
        wl.acquire()
    }
}
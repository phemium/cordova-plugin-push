package com.adobe.phonegap.push.utils

import com.adobe.phonegap.push.logs.Logger.Error
import com.adobe.phonegap.push.logs.Logger.Debug
import android.media.Ringtone
import android.media.RingtoneManager
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.Settings
import java.lang.NumberFormatException
import java.lang.reflect.InvocationTargetException

/**
 * Some custom ROMs and some Samsung Android 11 devices have quirks around accessing the default ringtone. This attempts to deal
 * with them with progressively worse approaches.
 */
object RingtoneUtil {
    private val TAG = RingtoneUtil::class.java.toString()
    fun getRingtone(context: Context, uri: Uri): Ringtone? {
        val tone: Ringtone? = try {
            RingtoneManager.getRingtone(context, uri)
        } catch (e: SecurityException) {
            Error(
                TAG,
                "getActualDefaultRingtoneUri",
                "Unable to get default ringtone due to permission: $e"
            )
            RingtoneManager.getRingtone(context, getActualDefaultRingtoneUri(context))
        }
        return tone
    }

    fun getActualDefaultRingtoneUri(context: Context): Uri? {
        Debug(
            TAG,
            "getActualDefaultRingtoneUri",
            "Attempting to get default ringtone directly via normal way"
        )
        try {
            return RingtoneManager.getActualDefaultRingtoneUri(
                context,
                RingtoneManager.TYPE_RINGTONE
            )
        } catch (e: SecurityException) {
            Error(
                TAG,
                "getActualDefaultRingtoneUri",
                "Failed to get ringtone with first fallback approach: $e"
            )
        }
        Debug(
            TAG,
            "getActualDefaultRingtoneUri",
            "Attempting to get default ringtone directly via reflection"
        )
        val uriString = getStringForUser(context.contentResolver, getUserId(context))
        var ringtoneUri = if (uriString != null) Uri.parse(uriString) else null
        if (ringtoneUri != null && getUserIdFromAuthority(
                ringtoneUri.authority,
                getUserId(context)
            ) == getUserId(context)
        ) {
            ringtoneUri = getUriWithoutUserId(ringtoneUri)
        }
        return ringtoneUri
    }

    @SuppressLint("DiscouragedPrivateApi")
    private fun getStringForUser(resolver: ContentResolver, userHandle: Int): String? {
        try {
            val getStringForUser = Settings.System::class.java.getMethod(
                "getStringForUser",
                ContentResolver::class.java,
                String::class.java,
                Int::class.javaPrimitiveType
            )
            return getStringForUser.invoke(
                Settings.System::class.java,
                resolver,
                Settings.System.RINGTONE,
                userHandle
            ) as String
        } catch (e: NoSuchMethodException) {
            Error(
                TAG,
                "getActualDefaultRingtoneUri",
                "Unable to getStringForUser via reflection: $e"
            )
        } catch (e: IllegalAccessException) {
            Error(
                TAG,
                "getActualDefaultRingtoneUri",
                "Unable to getStringForUser via reflection: $e"
            )
        } catch (e: InvocationTargetException) {
            Error(
                TAG,
                "getActualDefaultRingtoneUri",
                "Unable to getStringForUser via reflection: $e"
            )
        }
        return null
    }

    @SuppressLint("DiscouragedPrivateApi")
    private fun getUserId(context: Context): Int {
        try {
            val userId = Context::class.java.getMethod("getUserId").invoke(context)
            if (userId is Int) {
                return userId
            } else {
                Error(TAG, "getActualDefaultRingtoneUri", "getUserId did not return an integer")
            }
        } catch (e: IllegalAccessException) {
            Error(TAG, "getActualDefaultRingtoneUri", "Unable to getUserId via reflection: $e")
        } catch (e: InvocationTargetException) {
            Error(TAG, "getActualDefaultRingtoneUri", "Unable to getUserId via reflection: $e")
        } catch (e: NoSuchMethodException) {
            Error(TAG, "getActualDefaultRingtoneUri", "Unable to getUserId via reflection: $e")
        }
        return 0
    }

    private fun getUriWithoutUserId(uri: Uri?): Uri? {
        if (uri == null) {
            return null
        }
        val builder = uri.buildUpon()
        builder.authority(getAuthorityWithoutUserId(uri.authority))
        return builder.build()
    }

    private fun getAuthorityWithoutUserId(auth: String?): String? {
        if (auth == null) {
            return null
        }
        val end = auth.lastIndexOf('@')
        return auth.substring(end + 1)
    }

    private fun getUserIdFromAuthority(authority: String?, defaultUserId: Int): Int {
        if (authority == null) {
            return defaultUserId
        }
        val end = authority.lastIndexOf('@')
        if (end == -1) {
            return defaultUserId
        }
        val userIdString = authority.substring(0, end)
        return try {
            userIdString.toInt()
        } catch (e: NumberFormatException) {
            defaultUserId
        }
    }
}
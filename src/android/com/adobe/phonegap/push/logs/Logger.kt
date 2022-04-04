package com.adobe.phonegap.push.logs

import android.util.Log

object Logger {
    private var debugMode = false
    @JvmStatic
    fun setDebug(debug: Boolean) {
        debugMode = debug
    }

    @JvmStatic
    fun Error(tag: String?, origin: String, error: String) {
        Log.e(tag, getText(origin, error))
    }

    fun Debug(tag: String?, origin: String, text: String) {
        if (debugMode) Log.d(tag, getText(origin, text))
    }

    @JvmStatic
    fun Debug(tag: String?, origin: String, text: Int) {
        if (debugMode) Log.d(tag, getText(origin, text))
    }

    private fun getText(origin: String, text: String): String {
        return String.format(" [%s] %s", origin, text)
    }

    private fun getText(origin: String, text: Int): String {
        return String.format(" [%s] %s", origin, text)
    }
}
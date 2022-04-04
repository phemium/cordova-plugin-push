package com.adobe.phonegap.push.logs;

import android.util.Log;

public class Logger {
    private static Boolean debugMode = false;
    public static void setDebug(Boolean debug) {
        Logger.debugMode = debug;
    }
    public static void Error(String tag, String origin, String error) {
        Log.e(tag, Logger.getText(origin, error));
    }
    public static void Debug(String tag, String origin, String text) {
        if (Logger.debugMode) Log.d(tag, Logger.getText(origin, text));
    }
    public static void Debug(String tag, String origin, int text) {
        if (Logger.debugMode) Log.d(tag, Logger.getText(origin, text));
    }

    private static String getText(String origin, String text) {
        return String.format(" [%s] %s", origin, text);
    }

    private static String getText(String origin, int text) {
        return String.format(" [%s] %s", origin, text);
    }
}

package com.adobe.phonegap.push.utils;

import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;
import org.apache.cordova.CordovaInterface;

public class Globals {

    public static Context applicationContext;
    public static AppCompatActivity activity;;
    public static CordovaInterface cordova;
    // Contains the current language for the Main Application
    public static String language;

    public static void Initialize(CordovaInterface cordovaInterface) {
        cordova = cordovaInterface;
        activity = cordova.getActivity();
        applicationContext = activity.getApplicationContext();
    }

    public static void setLanguage(String lang) {
        language = lang;
    }
}

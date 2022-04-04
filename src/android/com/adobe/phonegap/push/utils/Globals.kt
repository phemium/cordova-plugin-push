package com.adobe.phonegap.push.utils

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import org.apache.cordova.CordovaInterface

object Globals {
    lateinit var applicationContext: Context
    lateinit var activity: AppCompatActivity
    lateinit var cordova: CordovaInterface

    // Contains the current language for the Main Application
    var language: String? = null
    fun Initialize(cordovaInterface: CordovaInterface) {
        cordova = cordovaInterface
        activity = cordova!!.activity
        applicationContext = activity!!.applicationContext
    }
}
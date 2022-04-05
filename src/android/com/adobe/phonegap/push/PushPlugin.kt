package com.adobe.phonegap.push

import android.R
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.Application.ActivityLifecycleCallbacks
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Resources.NotFoundException
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import com.adobe.phonegap.push.logs.Logger
import com.adobe.phonegap.push.notifications.*
import com.adobe.phonegap.push.notifications.channels.CallChannel
import com.adobe.phonegap.push.notifications.channels.MessageChannel
import com.adobe.phonegap.push.utils.CordovaCallbackContexts
import com.adobe.phonegap.push.utils.ServiceUtils
import com.adobe.phonegap.push.utils.Tools
import com.google.firebase.iid.FirebaseInstanceId
import org.apache.cordova.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*


/**
 * Cordova Plugin Push
 */
@Suppress("HardCodedStringLiteral")
@SuppressLint("LongLogTag", "LogConditional")
class PushPlugin : CordovaPlugin() {
  companion object {
    const val PREFIX_TAG: String = "cordova-plugin-push"
    private const val TAG: String = "$PREFIX_TAG (PushPlugin)"

    var language: String = "es"


    private val mCallbacks: MyActivityLifecycleCallbacks = MyActivityLifecycleCallbacks()

    var currentActivity: AppCompatActivity? = null

    var gWebView: CordovaWebView? = null
    private val gCachedExtras = Collections.synchronizedList(ArrayList<Bundle>())

    fun sendEvent(json: JSONObject?) {
      try {
        if (json != null) {
          Logger.Debug(TAG, "sendEvent", json.toString(4))
        }
      } catch (e: JSONException) {
        e.printStackTrace()
      }
      val pluginResult = PluginResult(PluginResult.Status.OK, json)
        .apply { keepCallback = true }
      sendPluginResultToCurrentActivity(pluginResult)
    }

    private fun sendPluginResultToCurrentActivity(pluginResult: PluginResult?) {
      val activityName = getCurrentActivityName()
      Log.d(TAG, "Sending event to current activity")
      val contexts: ArrayList<CallbackContext> = CordovaCallbackContexts.getCallbacksByActivity(activityName)
      Log.d(TAG, "Current activity has " + contexts.size + " callback contexts")
      var index = 0
      while (contexts.size > index) {
        val callbackContext = contexts[index]
        Log.d(
          TAG,
          "Sending pluginResult to CallbackContext #" + callbackContext.callbackId
        )
        callbackContext.sendPluginResult(pluginResult)
        index++
      }
    }

    fun sendError(message: String?) {
      val pluginResult = PluginResult(PluginResult.Status.ERROR, message)
      pluginResult.keepCallback = true
      sendPluginResultToCurrentActivity(pluginResult)
    }

    fun getCurrentActivityName(): String {
      return currentActivity!!.javaClass.simpleName
    }

    fun isInForeground(): Boolean {
      val appProcessInfo = RunningAppProcessInfo()
      ActivityManager.getMyMemoryState(appProcessInfo)
      val gForeground =
        appProcessInfo.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND || appProcessInfo.importance == RunningAppProcessInfo.IMPORTANCE_VISIBLE
      Log.d(TAG, "isInForeground: $gForeground")
      return gForeground
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @JvmStatic
    fun createLostCall(context: Context, extras: Bundle) {
      // Create Notification Builder
      val intent = Intent(context, CallNotificationActionReceiver::class.java)
      intent.putExtras(extras)
      intent.putExtra(PushConstants.CALL_RESPONSE_ACTION_KEY, PushConstants.VIEW_CHAT_ACTION)
      intent.action = PushConstants.VIEW_CHAT_ACTION
      val chatIntent = PendingIntent.getBroadcast(
        context,
        1204,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT
      )
      val notification = NotificationBuilder(context, MessageChannel.CHANNEL_ID, extras.getInt(PushConstants.EXTRA_CONSULTATION_ID))
      notification.setTitle("Llamada perdida")
      val description = extras.getString(PushConstants.EXTRA_DESCRIPTION)
      if (!description.isNullOrEmpty()) {
        notification.setText(description)
      }
      notification.setColor(extras.getString(PushConstants.EXTRA_COLOR))
      notification.setCloseOnClick(true)
      notification.setContentClickHandler(chatIntent)
      notification.setSmallIcon(R.drawable.sym_call_missed)
      notification.addActionButton(R.drawable.ic_menu_call, "Ir a la consulta", chatIntent)
      notification.show()
    }

    /**
     * Sends the push bundle extras to the client application. If the client
     * application isn't currently active and the no-cache flag is not set, it is
     * cached for later processing.
     *
     * @param extras
     */
    @JvmStatic
    fun sendExtras(extras: Bundle?) {
      /**
       * Serializes a bundle to JSON.
       *
       * @param extras
       *
       * @return JSONObject|null
       */
      fun convertBundleToJson(extras: Bundle): JSONObject? {
        Log.d(TAG, "Convert Extras to JSON")

        try {
          val json = JSONObject()
          val additionalData = JSONObject()

          // Add any keys that need to be in top level json to this set
          val jsonKeySet: HashSet<String?> = HashSet<String?>()

          Collections.addAll(
            jsonKeySet,
            PushConstants.TITLE,
            PushConstants.MESSAGE,
            PushConstants.COUNT,
            PushConstants.SOUND,
            PushConstants.IMAGE
          )

          val it: Iterator<String> = extras.keySet().iterator()

          while (it.hasNext()) {
            val key = it.next()
            val value = extras[key]

            Log.d(TAG, "Extras Iteration: key=$key")

            when {
              jsonKeySet.contains(key) -> {
                json.put(key, value)
              }

              key == PushConstants.COLDSTART -> {
                additionalData.put(key, extras.getBoolean(PushConstants.COLDSTART))
              }

              key == PushConstants.FOREGROUND -> {
                additionalData.put(key, extras.getBoolean(PushConstants.FOREGROUND))
              }

              key == PushConstants.DISMISSED -> {
                additionalData.put(key, extras.getBoolean(PushConstants.DISMISSED))
              }

              value is String -> {
                try {
                  // Try to figure out if the value is another JSON object
                  when {
                    value.startsWith("{") -> {
                      additionalData.put(key, JSONObject(value))
                    }

                    value.startsWith("[") -> {
                      additionalData.put(key, JSONArray(value))
                    }

                    else -> {
                      additionalData.put(key, value)
                    }
                  }
                } catch (e: Exception) {
                  additionalData.put(key, value)
                }
              }
            }
          }

          json.put(PushConstants.ADDITIONAL_DATA, additionalData)

          Log.v(TAG, "Extras To JSON Result: $json")
          return json
        } catch (e: JSONException) {
          Log.e(TAG, "convertBundleToJson had a JSON Exception")
        }

        return null
      }

      extras?.let {
        val noCache = it.getString(PushConstants.NO_CACHE)

        if (gWebView != null) {
          sendEvent(convertBundleToJson(extras))
        } else if (noCache != "1") {
          Log.v(TAG, "sendExtras: Caching extras to send at a later time.")
          gCachedExtras.add(extras)
        }
      }
    }

    /**
     * @return Boolean Active is true when the Cordova WebView is present.
     */
    val isActive: Boolean
      get() = gWebView != null


    private fun executeActionChangeLanguage(data: JSONArray, callbackContext: CallbackContext) {
      Logger.Debug(TAG, "executeActionChangeLanguage", "init")
      Logger.Debug(TAG, "executeActionChangeLanguage", data.toString())
      val language = data.getString(0)
      Logger.Debug(TAG, "executeActionChangeLanguage", "Execute Change Language to $language")
      this.language = language
      callbackContext.success()
    }
  }

  private val activity: Activity
    get() = cordova.activity

  val applicationContext: Context
    get() = activity.applicationContext

  @TargetApi(26)
  private fun createDefaultNotificationChannelIfNeeded() {
    // only call on Android O and above
    val messageChannelId = NotificationChannelManager.createIfNeeded(applicationContext, MessageChannel().asJSON(), true)
    MessageChannel.CHANNEL_ID = messageChannelId
    val callChannelId = NotificationChannelManager.createIfNeeded(applicationContext, CallChannel().asJSON(), true)
    CallChannel.CHANNEL_ID = callChannelId
  }

  /**
   * Performs various push plugin related tasks:
   *
   *  - Initialize
   *  - Unregister
   *  - Has Notification Permission Check
   *  - Set Icon Badge Number
   *  - Get Icon Badge Number
   *  - Clear All Notifications
   *  - Clear Notification
   *  - Subscribe
   *  - Unsubscribe
   *  - Create Channel
   *  - Delete Channel
   *  - List Channels
   *
   *  @param action
   *  @param data
   *  @param callbackContext
   */
  override fun execute(
    action: String,
    data: JSONArray,
    callbackContext: CallbackContext
  ): Boolean {
    Log.v(TAG, "Execute: Action = $action")

    gWebView = webView
    currentActivity = cordova.activity as AppCompatActivity

    when (action) {
      PushConstants.INITIALIZE -> executeActionInitialize(data, callbackContext)
      PushConstants.UNREGISTER -> executeActionUnregister(callbackContext)
      PushConstants.CHANGE_LANGUAGE -> executeActionChangeLanguage(data, callbackContext)
      PushConstants.FINISH -> callbackContext.success()
      PushConstants.HAS_PERMISSION -> executeActionHasPermission(callbackContext)
      PushConstants.SET_APPLICATION_ICON_BADGE_NUMBER -> executeActionSetIconBadgeNumber(
        data, callbackContext
      )
      PushConstants.GET_APPLICATION_ICON_BADGE_NUMBER -> executeActionGetIconBadgeNumber(
        callbackContext
      )
      PushConstants.CLEAR_ALL_NOTIFICATIONS -> executeActionClearAllNotifications(callbackContext)
      PushConstants.CLEAR_NOTIFICATION -> executeActionClearNotification(data, callbackContext)
      PushConstants.FINISH_CALL_REQUEST -> executeFinishCallRequest(callbackContext)
      else -> {
        Log.e(TAG, "Execute: Invalid Action $action")
        callbackContext.sendPluginResult(PluginResult(PluginResult.Status.INVALID_ACTION))
        return false
      }
    }
    return true
  }

  private fun executeFinishCallRequest(callbackContext: CallbackContext) {
    Logger.Debug(TAG, "executeFinishCallRequest", "init")
    cordova.threadPool.execute {
      try {
        applicationContext.stopService(Intent(applicationContext, CallNotificationService::class.java))
      } catch (e: java.lang.Exception) {
        callbackContext.error(e.message)
      }
      callbackContext.success()
    }
  }

  private fun executeActionInitialize(data: JSONArray, callbackContext: CallbackContext) {
    // Better Logging
    fun formatLogMessage(msg: String): String = "Execute::Initialize: ($msg)"

    cordova.threadPool.execute(Runnable {
      Log.v(TAG, formatLogMessage("Data=$data"))

      CordovaCallbackContexts.add(callbackContext)

      val sharedPref = applicationContext.getSharedPreferences(
        PushConstants.COM_ADOBE_PHONEGAP_PUSH,
        Context.MODE_PRIVATE
      )
      var initData: JSONObject? = null
      var token: String? = null
      var enduserToken: String? = null
      var environment: String? = null
      var senderID: String? = null

      try {
        initData = data.getJSONObject(0).getJSONObject(PushConstants.ANDROID)
        try {
          enduserToken = data.getJSONObject(0).getString("enduserToken")
          environment = data.getJSONObject(0).getString("environment")
          Logger.Error(TAG, "enduserToken", enduserToken)
          Logger.Error(TAG, "environment", environment)
          if (!enduserToken.isNullOrEmpty() && !environment.isNullOrEmpty()) {
            Logger.Debug(TAG, "executeActionInitialize", "Initializing PEM class")
            PEM.Initialize(enduserToken, environment)
          } else {
            Logger.Error(TAG, "executeActionInitialize", "Invalid enduserToken and/or environment")
          }
        } catch (e: java.lang.Exception) {
          Logger.Error(TAG, "executeActionInitialize", "You did not pass enduserToken and environment, please add it to .init() whenever possible.")
        }

        val senderIdResId = activity.resources.getIdentifier(
          PushConstants.GCM_DEFAULT_SENDER_ID,
          "string",
          activity.packageName
        )
        senderID = activity.getString(senderIdResId)

        // If no NotificationChannels exist create the default one
        createDefaultNotificationChannelIfNeeded()

        Log.v(TAG, formatLogMessage("JSONObject=$initData"))
        Log.v(TAG, formatLogMessage("senderID=$senderID"))

        try {
          token = FirebaseInstanceId.getInstance().token
        } catch (e: IllegalStateException) {
          Log.e(TAG, formatLogMessage("Firebase Token Exception ${e.message}"))
        }

        if (token == null) {
          try {
            token = FirebaseInstanceId.getInstance().getToken(senderID, PushConstants.FCM)
          } catch (e: IllegalStateException) {
            Log.e(TAG, formatLogMessage("Firebase Token Exception ${e.message}"))
          }
        }

        if (token != "") {
          val registration = JSONObject().put(PushConstants.REGISTRATION_ID, token).apply {
            put(PushConstants.REGISTRATION_TYPE, PushConstants.FCM)
          }

          Log.v(TAG, formatLogMessage("onRegistered=$registration"))

          sendEvent(registration)
        } else {
          callbackContext.error("Empty registration ID received from FCM")
          return@Runnable
        }
      } catch (e: JSONException) {
        Log.e(TAG, formatLogMessage("JSON Exception ${e.message}"))
        callbackContext.error(e.message)
      } catch (e: IOException) {
        Log.e(TAG, formatLogMessage("IO Exception ${e.message}"))
        callbackContext.error(e.message)
      } catch (e: NotFoundException) {
        Log.e(TAG, formatLogMessage("Resources NotFoundException Exception ${e.message}"))
        callbackContext.error(e.message)
      }

      initData?.let {
        /**
         * Add Shared Preferences
         *
         * Make sure to remove the preferences in the Remove step.
         */
        sharedPref.edit()?.apply {
          /**
           * Set Icon
           */
          try {
            putString(PushConstants.ICON, it.getString(PushConstants.ICON))
          } catch (e: JSONException) {
            Log.d(TAG, formatLogMessage("No Icon Options"))
          }

          /**
           * Set Icon Color
           */
          try {
            putString(PushConstants.ICON_COLOR, it.getString(PushConstants.ICON_COLOR))
          } catch (e: JSONException) {
            Log.d(TAG, formatLogMessage("No Icon Color Options"))
          }

          /**
           * Clear badge count when true
           */
          val clearBadge = it.optBoolean(PushConstants.CLEAR_BADGE, false)
          putBoolean(PushConstants.CLEAR_BADGE, clearBadge)

          if (clearBadge) {
              Tools.setApplicationIconBadgeNumber(applicationContext, 0)
          }

          /**
           * Set Sound
           */
          putBoolean(PushConstants.SOUND, it.optBoolean(PushConstants.SOUND, true))

          /**
           * Set Vibrate
           */
          putBoolean(PushConstants.VIBRATE, it.optBoolean(PushConstants.VIBRATE, true))

          /**
           * Set Clear Notifications
           */
          putBoolean(
            PushConstants.CLEAR_NOTIFICATIONS,
            it.optBoolean(PushConstants.CLEAR_NOTIFICATIONS, true)
          )

          /**
           * Set Force Show
           */
          putBoolean(
            PushConstants.FORCE_SHOW,
            it.optBoolean(PushConstants.FORCE_SHOW, false)
          )

          /**
           * Set SenderID
           */
          putString(PushConstants.SENDER_ID, senderID)

          /**
           * Set Message Key
           */
          putString(PushConstants.MESSAGE_KEY, it.optString(PushConstants.MESSAGE_KEY))

          /**
           * Set Title Key
           */
          putString(PushConstants.TITLE_KEY, it.optString(PushConstants.TITLE_KEY))

          apply()
        }
      }

      if (gCachedExtras.isNotEmpty()) {
        Log.v(TAG, formatLogMessage("Sending Cached Extras"))

        synchronized(gCachedExtras) {
          val gCachedExtrasIterator: Iterator<Bundle> = gCachedExtras.iterator()

          while (gCachedExtrasIterator.hasNext()) {
            sendExtras(gCachedExtrasIterator.next())
          }
        }

        gCachedExtras.clear()
      }
    })
  }

  private fun executeActionUnregister(callbackContext: CallbackContext) {
    // Better Logging
    fun formatLogMessage(msg: String): String = "Execute::Unregister: ($msg)"

    cordova.threadPool.execute {
      try {
        val sharedPref = applicationContext.getSharedPreferences(
          PushConstants.COM_ADOBE_PHONEGAP_PUSH,
          Context.MODE_PRIVATE
        )

        FirebaseInstanceId.getInstance().deleteInstanceId()
        Log.v(TAG, formatLogMessage("UNREGISTER"))

        /**
         * Remove Shared Preferences
         *
         * Make sure to remove what was in the Initialize step.
         */
        sharedPref.edit()?.apply {
          remove(PushConstants.ICON)
          remove(PushConstants.ICON_COLOR)
          remove(PushConstants.CLEAR_BADGE)
          remove(PushConstants.SOUND)
          remove(PushConstants.VIBRATE)
          remove(PushConstants.CLEAR_NOTIFICATIONS)
          remove(PushConstants.FORCE_SHOW)
          remove(PushConstants.SENDER_ID)
          remove(PushConstants.MESSAGE_KEY)
          remove(PushConstants.TITLE_KEY)

          apply()
        }

        callbackContext.success()
      } catch (e: IOException) {
        Log.e(TAG, formatLogMessage("IO Exception ${e.message}"))
        callbackContext.error(e.message)
      }
    }
  }

  private fun executeActionHasPermission(callbackContext: CallbackContext) {
    // Better Logging
    fun formatLogMessage(msg: String): String = "Execute::HasPermission: ($msg)"

    cordova.threadPool.execute {
      try {
        val isNotificationEnabled = NotificationManagerCompat.from(applicationContext)
          .areNotificationsEnabled()

        Log.d(TAG, formatLogMessage("Has Notification Permission: $isNotificationEnabled"))

        val jo = JSONObject().apply {
          put(PushConstants.IS_ENABLED, isNotificationEnabled)
        }

        val pluginResult = PluginResult(PluginResult.Status.OK, jo).apply {
          keepCallback = true
        }

        callbackContext.sendPluginResult(pluginResult)
      } catch (e: UnknownError) {
        callbackContext.error(e.message)
      } catch (e: JSONException) {
        callbackContext.error(e.message)
      }
    }
  }

  private fun executeActionSetIconBadgeNumber(data: JSONArray, callbackContext: CallbackContext) {
    fun formatLogMessage(msg: String): String = "Execute::SetIconBadgeNumber: ($msg)"

    cordova.threadPool.execute {
      Log.v(TAG, formatLogMessage("data=$data"))

      try {
        val badgeCount = data.getJSONObject(0).getInt(PushConstants.BADGE)
          Tools.setApplicationIconBadgeNumber(applicationContext, badgeCount)
      } catch (e: JSONException) {
        callbackContext.error(e.message)
      }

      callbackContext.success()
    }
  }

  private fun executeActionGetIconBadgeNumber(callbackContext: CallbackContext) {
    cordova.threadPool.execute {
      Log.v(TAG, "Execute::GetIconBadgeNumber")
      callbackContext.success(Tools.getApplicationIconBadgeNumber(applicationContext))
    }
  }

  private fun executeActionClearAllNotifications(callbackContext: CallbackContext) {
    cordova.threadPool.execute {
      Log.v(TAG, "Execute Clear All Notifications")
      ServiceUtils.notificationService(applicationContext).cancelAll()
      callbackContext.success()
    }
  }

  private fun executeActionClearNotification(data: JSONArray, callbackContext: CallbackContext) {
    cordova.threadPool.execute {
      try {
        val notificationId = data.getInt(0)
        Log.v(TAG, "Execute::ClearNotification notificationId=$notificationId")
        val notificationManager = ServiceUtils.notificationService(applicationContext)
        notificationManager.cancel(notificationId)
        callbackContext.success()
      } catch (e: JSONException) {
        callbackContext.error(e.message)
      }
    }
  }

  /**
   * Initialize
   */
  override fun initialize(cordova: CordovaInterface, webView: CordovaWebView) {
    Logger.setDebug(true)
    super.initialize(cordova, webView)
  }

  override fun pluginInitialize() {
    cordova.activity.application.registerActivityLifecycleCallbacks(mCallbacks)
    super.pluginInitialize()
  }

  /**
   * Handle when the view is being destroyed
   */
  override fun onDestroy() {

    // Clear Notification
    applicationContext.getSharedPreferences(
      PushConstants.COM_ADOBE_PHONEGAP_PUSH,
      Context.MODE_PRIVATE
    ).apply {
      if (getBoolean(PushConstants.CLEAR_NOTIFICATIONS, true)) {
        ServiceUtils.notificationService(applicationContext).cancelAll()
      }
    }
    cordova.activity.application.unregisterActivityLifecycleCallbacks(mCallbacks)
    super.onDestroy()
  }

  class MyActivityLifecycleCallbacks : ActivityLifecycleCallbacks {
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
      val activityName = activity.javaClass.simpleName
      Log.d(TAG, "Created activity: $activityName")
      CordovaCallbackContexts.create(activityName)
    }

    override fun onActivityStarted(activity: Activity) {
      val activityName = activity.javaClass.simpleName
      LOG.d(TAG, "Started activity: $activityName")
    }

    override fun onActivityResumed(activity: Activity) {
      val activityName = activity.javaClass.simpleName
      LOG.d(TAG, "Resumed activity: $activityName")
      try {
        currentActivity = activity as AppCompatActivity
      } catch (e: Exception) {
        LOG.e(TAG, "Unable to cast new activity as AppCompatActivity")
      }
    }

    override fun onActivityPaused(activity: Activity) {
      val activityName = activity.javaClass.simpleName
      LOG.d(TAG, "Paused activity: $activityName")
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityStopped(activity: Activity) {
      val activityName = activity.javaClass.simpleName
      LOG.d(TAG, "Stopped activity: $activityName")
    }

    override fun onActivityDestroyed(activity: Activity) {
      val activityName = activity.javaClass.simpleName
      LOG.d(TAG, "Destroying activity: $activityName")
      CordovaCallbackContexts.remove((activityName))
    }

    companion object {
      private const val TAG = "Activities"
    }
  }
}

package com.adobe.phonegap.push

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.app.RemoteInput
import com.adobe.phonegap.push.firebase.MessagingService
import com.adobe.phonegap.push.logs.Logger
import com.adobe.phonegap.push.utils.ServiceUtils

/**
 * Background Handler Activity
 */
@Suppress("HardCodedStringLiteral")
@SuppressLint("LongLogTag", "LogConditional")
class BackgroundHandlerActivity : Activity() {
  companion object {
    private const val TAG: String = "${PushPlugin.PREFIX_TAG} (BackgroundHandlerActivity)"
  }

  /**
   * This activity will be started if the user touches a notification that we own.
   * We send it's data off to the push plugin for processing.
   * If needed, we boot up the main activity to kickstart the application.
   *
   * @param savedInstanceState
   *
   * @see android.app.Activity#onCreate(android.os.Bundle)
   */
  public override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    Log.v(TAG, "onCreate")

    intent.extras?.let { extras ->
      val notId = extras.getInt(PushConstants.NOT_ID, 0)
      val callback = extras.getString(PushConstants.CALLBACK)
      val startOnBackground = extras.getBoolean(PushConstants.START_IN_BACKGROUND, false)
      val dismissed = extras.getBoolean(PushConstants.DISMISSED, false)

      Logger.Debug(TAG, "onCreate","Not ID: $notId")
      Logger.Debug(TAG, "onCreate", "Callback: $callback ")
      Logger.Debug(TAG, "onCreate", "Start In Background: $startOnBackground")
      Logger.Debug(TAG, "onCreate", "Dismissed: $dismissed")

      MessagingService().setNotification(notId, "")

      if (!startOnBackground) {
        val notificationManager = ServiceUtils.notificationService(this.applicationContext)
        notificationManager.cancel(MessagingService.getAppName(this), notId)
      }

      processPushBundle()
      finish()

      if (!dismissed) {
        // Tap the notification, app should start.
        if (!PushPlugin.isActive) {
          forceMainActivityReload(false)
        } else {
          forceMainActivityReload(true)
        }
      }
    }
  }

  private fun processPushBundle() {
    /*
     * Takes the pushBundle extras from the intent,
     * and sends it through to the PushPlugin for processing.
     */
    intent.extras?.let { extras ->
      var originalExtras = extras.getBundle(PushConstants.PUSH_BUNDLE)

      if (originalExtras == null) {
        originalExtras = extras
        originalExtras.remove(PushConstants.FROM)
        originalExtras.remove(PushConstants.MESSAGE_ID)
        originalExtras.remove(PushConstants.COLLAPSE_KEY)
      }

      originalExtras.putBoolean(PushConstants.FOREGROUND, false)
      originalExtras.putBoolean(PushConstants.COLDSTART, !PushPlugin.isActive)
      originalExtras.putBoolean(PushConstants.DISMISSED, extras.getBoolean(PushConstants.DISMISSED))
      originalExtras.putString(
        PushConstants.ACTION_CALLBACK,
        extras.getString(PushConstants.CALLBACK)
      )
      originalExtras.remove(PushConstants.NO_CACHE)

      RemoteInput.getResultsFromIntent(intent)?.apply {
        val reply = getCharSequence(PushConstants.INLINE_REPLY).toString()
        Logger.Debug(TAG, "processPushBundle", "Inline Reply: $reply")

        originalExtras.putString(PushConstants.INLINE_REPLY, reply)
      }

      PushPlugin.sendExtras(originalExtras)
    }
  }

  private fun forceMainActivityReload(startOnBackground: Boolean) {
    /*
     * Forces the main activity to re-launch if it's unloaded.
     */
    val launchIntent = packageManager.getLaunchIntentForPackage(applicationContext.packageName)

    intent.extras?.let { extras ->
      launchIntent?.apply {
        extras.getBundle(PushConstants.PUSH_BUNDLE)?.let { originalExtras ->
          putExtras(originalExtras)
        }

        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        addFlags(Intent.FLAG_FROM_BACKGROUND)
        putExtra(PushConstants.START_IN_BACKGROUND, startOnBackground)
      }
    }

    startActivity(launchIntent)
  }

  /**
   *
   */
  override fun onResume() {
    super.onResume()

    val notificationManager = ServiceUtils.notificationService(this.applicationContext)
    notificationManager.cancelAll()
  }
}

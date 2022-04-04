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
 * Push Handler Activity
 */
@Suppress("HardCodedStringLiteral")
@SuppressLint("LongLogTag", "LogConditional")
class PushHandlerActivity : Activity() {
  companion object {
    private const val TAG: String = "${PushPlugin.PREFIX_TAG} (PushHandlerActivity)"
  }

  /**
   * this activity will be started if the user touches a notification that we own.
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
      var foreground = extras.getBoolean(PushConstants.FOREGROUND, true)
      val startOnBackground = extras.getBoolean(PushConstants.START_IN_BACKGROUND, false)
      val dismissed = extras.getBoolean(PushConstants.DISMISSED, false)

      MessagingService().setNotification(notId, "")

      if (!startOnBackground) {
        val notificationManager = ServiceUtils.notificationService(this.applicationContext)
        notificationManager.cancel(MessagingService.getAppName(this), notId)
      }

      val notHaveInlineReply = processPushBundle()

      if (notHaveInlineReply && !startOnBackground) {
        foreground = true
      }

      Logger.Debug(TAG, "onCreate", "Not ID: $notId")
      Logger.Debug(TAG, "onCreate", "Callback: $callback")
      Logger.Debug(TAG, "onCreate", "Foreground: $foreground")
      Logger.Debug(TAG, "onCreate", "Start On Background: $startOnBackground")
      Logger.Debug(TAG, "onCreate", "Dismissed: $dismissed")

      finish()

      if (!dismissed) {
        Logger.Debug(TAG, "onCreate", "Is Push Plugin Active: ${PushPlugin.isActive}")

        if (!PushPlugin.isActive && foreground && notHaveInlineReply) {
          Logger.Debug(TAG, "onCreate", "Force Main Activity Reload: Start on Background = False")
          forceMainActivityReload(false)
        } else if (startOnBackground) {
          Logger.Debug(TAG, "onCreate", "Force Main Activity Reload: Start on Background = True")
          forceMainActivityReload(true)
        } else {
          Logger.Debug(TAG, "onCreate", "Don't Want Main Activity")
        }
      }
    }
  }

  private fun processPushBundle(): Boolean {
    /*
     * Takes the pushBundle extras from the intent,
     * and sends it through to the PushPlugin for processing.
     */
    return intent.extras?.let { extras ->
      var notHaveInlineReply = true

      extras.getBundle(PushConstants.PUSH_BUNDLE)?.apply {
        putBoolean(PushConstants.FOREGROUND, false)
        putBoolean(PushConstants.COLDSTART, !PushPlugin.isActive)
        putBoolean(PushConstants.DISMISSED, extras.getBoolean(PushConstants.DISMISSED))
        putString(
          PushConstants.ACTION_CALLBACK,
          extras.getString(PushConstants.CALLBACK)
        )
        remove(PushConstants.NO_CACHE)

        RemoteInput.getResultsFromIntent(intent)?.let { results ->
          val reply = results.getCharSequence(PushConstants.INLINE_REPLY).toString()
          Logger.Debug(TAG, "processPushBundle", "Inline Reply: $reply")

          putString(PushConstants.INLINE_REPLY, reply)
          notHaveInlineReply = false
        }

        PushPlugin.sendExtras(this)
      }

      return notHaveInlineReply
    } ?: true
  }

  private fun forceMainActivityReload(startOnBackground: Boolean) {
    /*
     * Forces the main activity to re-launch if it's unloaded.
     */
    val launchIntent = packageManager.getLaunchIntentForPackage(applicationContext.packageName)

    intent.extras?.let { extras ->
      launchIntent?.apply {
        extras.getBundle(PushConstants.PUSH_BUNDLE)?.apply {
          putExtras(this)
        }

        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        addFlags(Intent.FLAG_FROM_BACKGROUND)
        putExtra(PushConstants.START_IN_BACKGROUND, startOnBackground)
      }
    }

    startActivity(launchIntent)
  }

  /**
   * On Resuming of Activity
   */
  override fun onResume() {
    super.onResume()

    val notificationManager = ServiceUtils.notificationService(this.applicationContext)
    notificationManager.cancelAll()
  }
}

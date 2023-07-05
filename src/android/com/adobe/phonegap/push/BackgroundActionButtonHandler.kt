package com.adobe.phonegap.push

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import com.adobe.phonegap.push.firebase.MessagingService
import com.adobe.phonegap.push.logs.Logger
import com.adobe.phonegap.push.utils.ServiceUtils

/**
 * Background Action Button Handler
 */
@Suppress("HardCodedStringLiteral")
@SuppressLint("LongLogTag", "LogConditional")
class BackgroundActionButtonHandler : BroadcastReceiver() {
  companion object {
    private const val TAG: String = "${PushPlugin.PREFIX_TAG} (BackgroundActionButtonHandler)"
  }

  /**
   * @param context
   * @param intent
   */
  override fun onReceive(context: Context, intent: Intent) {
    val notId = intent.getIntExtra(PushConstants.NOT_ID, 0)
    Logger.Debug(TAG, "onReceive", "Not ID: $notId")

    val notificationManager = ServiceUtils.notificationService(context)
    notificationManager.cancel(MessagingService.getAppName(context), notId)

    intent.extras?.let { extras ->
      Logger.Debug(TAG, "onReceive", "Intent Extras: $extras")
      extras.getBundle(PushConstants.PUSH_BUNDLE)?.apply {
        putBoolean(PushConstants.FOREGROUND, false)
        putBoolean(PushConstants.COLDSTART, false)
        putString(
          PushConstants.ACTION_CALLBACK,
          extras.getString(PushConstants.CALLBACK)
        )

        RemoteInput.getResultsFromIntent(intent)?.let { remoteInputResults ->
          val results = remoteInputResults.getCharSequence(PushConstants.INLINE_REPLY).toString()
          Logger.Debug(TAG, "onReceive", "Inline Reply: $results")

          putString(PushConstants.INLINE_REPLY, results)
        }
      }

      PushPlugin.sendExtras(extras)
    }
  }
}

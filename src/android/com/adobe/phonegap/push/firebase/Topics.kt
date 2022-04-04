package com.adobe.phonegap.push.firebase

import com.adobe.phonegap.push.PushPlugin
import kotlin.Throws
import org.json.JSONException
import com.adobe.phonegap.push.logs.Logger
import com.google.firebase.messaging.FirebaseMessaging

object Topics {
    private const val TAG = PushPlugin.PREFIX_TAG + " FirebaseTopics"
    @Throws(JSONException::class)
    fun subscribeTo(topics: Array<String?>?) {
        if (topics != null) {
            for (i in topics.indices) {
                val topicKey = topics[i]
                subscribeTo(topicKey)
            }
        }
    }

    private fun unsubscribeFrom(topics: Array<String>?) {
        if (topics != null) {
            for (i in topics.indices) {
                val topicKey = topics[i]
                unsubscribeFrom(topicKey)
            }
        }
    }

    private fun subscribeTo(topic: String?) {
        if (topic != null) {
            Logger.Debug(TAG, "subscribeToTopic", "Subscribing to Topic: $topic")
            FirebaseMessaging.getInstance().subscribeToTopic(topic)
        }
    }

    private fun unsubscribeFrom(topic: String?) {
        if (topic != null) {
            Logger.Debug(TAG, "subscribeToTopic", "Subscribing to Topic: $topic")
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
        }
    }
}
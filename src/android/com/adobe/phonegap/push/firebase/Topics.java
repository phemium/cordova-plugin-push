package com.adobe.phonegap.push.firebase;

import com.adobe.phonegap.push.PushPlugin;
import com.adobe.phonegap.push.logs.Logger;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;

public class Topics {

    private static String TAG = PushPlugin.PREFIX_TAG + " FirebaseTopics";

    public static void subscribeTo(String[] topics) throws JSONException {
        if (topics != null) {
            for (int i = 0; i < topics.length; i++) {
                String topicKey = topics[i];
                subscribeTo(topicKey);
            }
        }
    }

    private static void unsubscribeFrom(String[] topics) {
        if (topics != null) {
            for (int i = 0; i < topics.length; i++) {
                String topicKey = topics[i];
                unsubscribeFrom(topicKey);
            }
        }
    }

    private static void subscribeTo(String topic) {
        if (topic != null) {
            Logger.Debug(TAG, "subscribeToTopic", "Subscribing to Topic: " + topic);
            FirebaseMessaging.getInstance().subscribeToTopic(topic);
        }
    }

    private static void unsubscribeFrom(String topic) {
        if (topic != null) {
            Logger.Debug(TAG, "subscribeToTopic", "Subscribing to Topic: " + topic);
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic);
        }
    }
}

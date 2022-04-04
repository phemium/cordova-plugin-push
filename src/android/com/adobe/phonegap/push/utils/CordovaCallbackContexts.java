package com.adobe.phonegap.push.utils;

import com.adobe.phonegap.push.PushPlugin;
import com.adobe.phonegap.push.logs.Logger;

import org.apache.cordova.CallbackContext;
import java.util.ArrayList;
import java.util.HashMap;

public class CordovaCallbackContexts {

    private static String TAG = PushPlugin.PREFIX_TAG + " (PushPlugin)";

    /**
     * Contains all the callback contexts for each Activity used by the application
     */
    private static HashMap<String, ArrayList<CallbackContext>> callbackContextsMap = new HashMap<>();

    public static ArrayList<CallbackContext> getCallbacksByActivity(String activityName) {
        create(activityName);
        return callbackContextsMap.get(activityName);
    }

    public static void create(String activityName) {
        if (!callbackContextsMap.containsKey(activityName)) {
            callbackContextsMap.put(activityName, new ArrayList<>());
        }
    }

    public static void remove(String activityName) {
        if (callbackContextsMap.containsKey(activityName)) {
            callbackContextsMap.remove(activityName);
        }
    }

    public static void add(CallbackContext callbackContext) {
        String activityName = PushPlugin.Companion.getCurrentActivityName();
        Logger.setDebug(true);
        ArrayList<CallbackContext> contexts = getCallbacksByActivity(activityName);
        int index = 0;
        int foundCallbackContext = -1;
        while (contexts.size() > index) {
            if (contexts.get(index).getCallbackId() == callbackContext.getCallbackId()) {
                foundCallbackContext = index;
                break;
            }
            index++;
        }
        if (foundCallbackContext == -1) {
            Logger.Debug(
                    TAG,
                    "addCallbackContent",
                    "Registering new CallbackContext #" + callbackContext.getCallbackId() + " inside Activity " + activityName
            );
            contexts.add(callbackContext);
        }
    }
}

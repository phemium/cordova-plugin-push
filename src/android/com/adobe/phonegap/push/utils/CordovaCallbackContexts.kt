package com.adobe.phonegap.push.utils

import com.adobe.phonegap.push.PushPlugin.Companion.getCurrentActivityName
import com.adobe.phonegap.push.logs.Logger.setDebug
import com.adobe.phonegap.push.logs.Logger.Debug
import com.adobe.phonegap.push.PushPlugin
import org.apache.cordova.CallbackContext
import java.util.ArrayList
import java.util.HashMap

object CordovaCallbackContexts {
    private const val TAG = PushPlugin.PREFIX_TAG + " (PushPlugin)"

    /**
     * Contains all the callback contexts for each Activity used by the application
     */
    private val callbackContextsMap = HashMap<String, ArrayList<CallbackContext>>()
    fun getCallbacksByActivity(activityName: String): ArrayList<CallbackContext> {
        create(activityName)
        return callbackContextsMap[activityName]!!
    }

    fun create(activityName: String) {
        if (!callbackContextsMap.containsKey(activityName)) {
            callbackContextsMap[activityName] = ArrayList()
        }
    }

    fun remove(activityName: String) {
        if (callbackContextsMap.containsKey(activityName)) {
            callbackContextsMap.remove(activityName)
        }
    }

    fun add(callbackContext: CallbackContext) {
        val activityName = getCurrentActivityName()
        setDebug(true)
        val contexts = getCallbacksByActivity(activityName)
        var index = 0
        var foundCallbackContext = -1
        while (contexts.size > index) {
            if (contexts[index].callbackId === callbackContext.callbackId) {
                foundCallbackContext = index
                break
            }
            index++
        }
        if (foundCallbackContext == -1) {
            Debug(
                TAG,
                "addCallbackContent",
                "Registering new CallbackContext #" + callbackContext.callbackId + " inside Activity " + activityName
            )
            contexts.add(callbackContext)
        }
    }
}
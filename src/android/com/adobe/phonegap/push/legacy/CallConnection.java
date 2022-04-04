package com.adobe.phonegap.push.legacy;

import android.content.Intent;
import android.telecom.CallAudioState;
import android.telecom.Connection;
import android.telecom.DisconnectCause;
import android.util.Log;

import com.adobe.phonegap.push.PushPlugin;
import com.adobe.phonegap.push.notifications.CallNotificationService;
import com.adobe.phonegap.push.utils.Globals;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;

import java.util.ArrayList;

/**
 * @deprecated
 */
public class CallConnection extends Connection {

    private static String TAG = PushPlugin.PREFIX_TAG + " (CallConnection)";

    @Override
    public void onAnswer() {
        Log.d(TAG, "Incoming - onAnswer");
        Log.d(TAG, "Incoming - Setting to active");
        this.setActive();
        Intent intent = new Intent(CordovaCall.getCordova().getActivity().getApplicationContext(), CordovaCall.getCordova().getActivity().getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        CordovaCall.getCordova().getActivity().getApplicationContext().startActivity(intent);
        Log.d(TAG, "Incoming - Sending onAnswer to all listeners");
        ArrayList<CallbackContext> callbackContexts = CordovaCall.getCallbackContexts().get("answer");
        for (final CallbackContext callbackContext : callbackContexts) {
            CordovaCall.getCordova().getThreadPool().execute(new Runnable() {
                public void run() {
                    PluginResult result = new PluginResult(PluginResult.Status.OK, "answer event called successfully");
                    result.setKeepCallback(true);
                    callbackContext.sendPluginResult(result);
                }
            });
        }
    }

    @Override
    public void onReject() {
        Log.d(TAG, "Incoming - onReject");
        DisconnectCause cause = new DisconnectCause(DisconnectCause.REJECTED);
        this.setDisconnected(cause);
        this.destroy();
        ArrayList<CallbackContext> callbackContexts = CordovaCall.getCallbackContexts().get("reject");
        for (final CallbackContext callbackContext : callbackContexts) {
            CordovaCall.getCordova().getThreadPool().execute(new Runnable() {
                public void run() {
                    PluginResult result = new PluginResult(PluginResult.Status.OK, "reject event called successfully");
                    result.setKeepCallback(true);
                    callbackContext.sendPluginResult(result);
                }
            });
        }
    }

    @Override
    public void onHold() {
        Log.d(TAG, "Ingoing - onHold");
        super.onHold();
    }

    @Override
    public void onAbort() {
        Log.d(TAG, "Incoming - onAbort");
        super.onAbort();
    }

    @Override
    public void onDisconnect() {
        Log.d(TAG, "Incoming - onDisconnect");
        DisconnectCause cause = new DisconnectCause(DisconnectCause.LOCAL);
        this.setDisconnected(cause);
        this.destroy();
        ArrayList<CallbackContext> callbackContexts = CordovaCall.getCallbackContexts().get("hangup");
        for (final CallbackContext callbackContext : callbackContexts) {
            CordovaCall.getCordova().getThreadPool().execute(new Runnable() {
                public void run() {
                    PluginResult result = new PluginResult(PluginResult.Status.OK, "hangup event called successfully");
                    result.setKeepCallback(true);
                    callbackContext.sendPluginResult(result);
                }
            });
        }
    }

    public void disconnectAndDestroy() {
        setDisconnected(new DisconnectCause(DisconnectCause.LOCAL));
        destroy();
    }

    @Override
    public void onCallAudioStateChanged(CallAudioState state) {
        super.onCallAudioStateChanged(state);
    }

    @Override
    public void onShowIncomingCallUi() {
        Intent serviceIntent = new Intent(Globals.applicationContext, CallNotificationService.class);
        serviceIntent.putExtra("consultant", "Celia Creu");
        Globals.applicationContext.startService(serviceIntent);
    }

}
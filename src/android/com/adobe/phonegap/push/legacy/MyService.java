package com.adobe.phonegap.push.legacy;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;

import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.telecom.Connection;
import android.telecom.ConnectionRequest;
import android.telecom.ConnectionService;
import android.telecom.PhoneAccountHandle;
import android.telecom.StatusHints;
import android.telecom.TelecomManager;
import android.net.Uri;
import java.util.ArrayList;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.adobe.phonegap.push.PushPlugin;

/**
 * @deprecated
 */
public class MyService extends ConnectionService {

    private static String TAG = PushPlugin.PREFIX_TAG + " (MyConnectionService)";
    private static Connection conn;

    public static Connection getConnection() {
        return conn;
    }

    public static void deinitConnection() {
        conn = null;
    }

    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    @Override
    public Connection onCreateIncomingConnection(final PhoneAccountHandle connectionManagerPhoneAccount, final ConnectionRequest request) {
        Log.d(TAG, "Incoming - onCreateIncomingConnection");
        final Connection connection = new CallConnection();
        Bundle phoneAccountExtras = request.getExtras();
        connection.setConnectionProperties(Connection.PROPERTY_SELF_MANAGED);
        connection.setConnectionCapabilities(connection.getConnectionCapabilities() | 0x2);
        connection.setInitializing();
        connection.setActive();
        connection.setAudioModeIsVoip(true);
        connection.setExtras(phoneAccountExtras);
        connection.setAddress(Uri.parse(request.getExtras().getString("from")), TelecomManager.PRESENTATION_ALLOWED);
        Icon icon = CordovaCall.getIcon();
        if(icon != null) {
            StatusHints statusHints = new StatusHints((CharSequence)"", icon, new Bundle());
            connection.setStatusHints(statusHints);
        }
        ArrayList<CallbackContext> callbackContexts = CordovaCall.getCallbackContexts().get("receiveCall");
        if (callbackContexts != null) {
            for (final CallbackContext callbackContext : callbackContexts) {
                CordovaCall.getCordova().getThreadPool().execute(new Runnable() {
                    public void run() {
                        PluginResult result = new PluginResult(PluginResult.Status.OK, "receiveCall event called successfully");
                        result.setKeepCallback(true);
                        callbackContext.sendPluginResult(result);
                    }
                });
            }
        }
        Log.d(TAG, "Ingoing - Returned connection");
        return connection;
    }

    @Override
    public void onCreateIncomingConnectionFailed(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        super.onCreateIncomingConnectionFailed(connectionManagerPhoneAccount, request);
    }

    @Override
    public void onCreateOutgoingConnectionFailed(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        super.onCreateOutgoingConnectionFailed(connectionManagerPhoneAccount, request);
    }

    @Override
    public Connection onCreateOutgoingConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        return super.onCreateOutgoingConnection(connectionManagerPhoneAccount, request);
    }
}
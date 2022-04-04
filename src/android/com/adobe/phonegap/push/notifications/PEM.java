package com.adobe.phonegap.push.notifications;

import android.os.Bundle;

public class PEM {

    public static String enduserToken = "";
    public static String environment = "prerelease";

    public static String constructUrlParams(Bundle params) {
        String urlParams = "";
        for (String key : params.keySet()) {
            String pair = key + "=" + params.get(key).toString();
            if (!urlParams.equals("")) {
                pair = "&" + pair;
            }
            urlParams += pair;
        }
        return urlParams;
    }

    public static void Initialize(String token, String env) {
        enduserToken = token;
        environment = env;
    }

    public static String getEnduserParamsForChat(int consultationId) {
        Bundle params = new Bundle();
        params.putString("enduser_token", enduserToken);
        params.putInt("consultation_id", consultationId);
        params.putString("environment", environment);
        params.putString("origin", "notification");
        params.putBoolean("face2face", false);
        return constructUrlParams(params);
    }

    public static String getEnduserParamsForCall(int consultationId, Boolean acceptCall) {
        Bundle params = new Bundle();
        params.putString("enduser_token", enduserToken);
        params.putInt("consultation_id", consultationId);
        params.putString("environment", environment);
        params.putBoolean("face2face", false);
        params.putString("action", "call_request");
        params.putString("origin", "notification");
        if (acceptCall != null) {
            params.putBoolean("accept_call", acceptCall);
        }
        return constructUrlParams(params);
    }
}

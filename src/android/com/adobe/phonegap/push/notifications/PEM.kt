package com.adobe.phonegap.push.notifications

import android.os.Bundle

object PEM {
    var enduserToken = ""
    var environment = "prerelease"
    fun constructUrlParams(params: Bundle): String {
        var urlParams = ""
        for (key in params.keySet()) {
            var pair = key + "=" + params[key].toString()
            if (urlParams != "") {
                pair = "&$pair"
            }
            urlParams += pair
        }
        return urlParams
    }

    fun Initialize(token: String, env: String) {
        enduserToken = token
        environment = env
    }

    fun getEnduserParamsForChat(consultationId: Int): String {
        val params = Bundle()
        params.putString("enduser_token", enduserToken)
        params.putInt("consultation_id", consultationId)
        params.putString("environment", environment)
        params.putString("origin", "notification")
        params.putBoolean("face2face", false)
        return constructUrlParams(params)
    }

    fun getEnduserParamsForCall(consultationId: Int, acceptCall: Boolean?): String {
        val params = Bundle()
        params.putString("enduser_token", enduserToken)
        params.putInt("consultation_id", consultationId)
        params.putString("environment", environment)
        params.putBoolean("face2face", false)
        params.putString("action", "call_request")
        params.putString("origin", "notification")
        if (acceptCall != null) {
            params.putBoolean("accept_call", acceptCall)
        }
        return constructUrlParams(params)
    }
}
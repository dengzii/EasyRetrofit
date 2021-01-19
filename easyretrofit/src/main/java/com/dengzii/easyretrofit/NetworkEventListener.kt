package com.dengzii.easyretrofit

import okhttp3.Call
import okhttp3.EventListener
import java.io.IOException


class NetworkEventListener : EventListener() {
    override fun callStart(call: Call) {
        super.callStart(call)
    }

    override fun callEnd(call: Call) {
        super.callEnd(call)
    }

    override fun callFailed(call: Call, ioe: IOException) {
        super.callFailed(call, ioe)
    }
}
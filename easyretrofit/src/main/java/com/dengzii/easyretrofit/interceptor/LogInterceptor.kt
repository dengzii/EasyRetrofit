package com.dengzii.easyretrofit.interceptor

import com.dengzii.easyretrofit.NetworkException
import com.dengzii.easyretrofit.RetrofitManager
import com.dengzii.easyretrofit.interfaces.Logger
import com.dengzii.easyretrofit.interfaces.NetworkLogger
import com.google.gson.JsonParser
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okio.Buffer
import java.io.IOException

class LogInterceptor(private val mLogger: NetworkLogger) : Interceptor {

    companion object {
        private const val TAG = "LogInterceptor"
        private val TYPE_JSON: MediaType = "application/json; charset=UTF-8".toMediaType()
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originRequest = chain.request()

        if (mLogger.level != Logger.NONE) {
            mLogger.i(
                Logger.REQUEST_TYPE, TAG, "${originRequest.method} ${originRequest.url}"
            )
            logHeaders(originRequest)
            logParameter(originRequest)
        }
        val response = chain.proceed(originRequest)

        if (!response.isSuccessful) {
            val exception = NetworkException(response.code, response.message)
            mLogger.e(Logger.GENERAL, TAG, exception)
        }
        mLogger.i(
            Logger.RESPONSE_TYPE, TAG,
            "${response.request.method} ${response.request.url}" +
                    " ${response.code} ${response.message} ${response.body?.contentType()}"
        )
        return response
    }

    private fun logParameter(request: Request) {
        var log = ""
        val requestBody = request.body

        if (requestBody is FormBody) {
            val stringBuilder = StringBuilder()
            stringBuilder.append("{\n")
            val size = requestBody.size
            for (i in 0 until size) {
                stringBuilder.append("\t ${requestBody.name(i)}: ${requestBody.value(i)},\n")
            }
            stringBuilder.trimEnd('\n')
            stringBuilder.trimEnd(',')
            stringBuilder.append("\n}")
            log = stringBuilder.toString().replace("{\n}", "{}")

        } else if (TYPE_JSON == requestBody?.contentType()) {
            val buffer = Buffer()
            try {
                requestBody.writeTo(buffer)
                val cnt = JsonParser.parseString(buffer.readUtf8())
                log = RetrofitManager.getInstance().gson.toJson(cnt)
            } catch (e: IOException) {
                mLogger.e(Logger.GENERAL, TAG, e)
            }
        }
        mLogger.i(Logger.REQUEST_BODY, TAG, "Request Parameters: $log")
    }

    private fun logHeaders(request: Request) {
        val headers = request.headers
        val builder = StringBuilder()
        builder.append("Request Headers:")
        if (headers.size > 0) {
            builder.append("\n")
        }
        for (key in headers.names()) {
            builder.append("$key: ${headers[key]}")
        }
        mLogger.i(
            Logger.REQUEST_HEADERS,
            TAG,
            builder.toString()
        )
    }
}
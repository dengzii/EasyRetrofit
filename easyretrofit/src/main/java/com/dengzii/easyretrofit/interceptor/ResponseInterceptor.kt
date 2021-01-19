package com.dengzii.easyretrofit.interceptor

import com.dengzii.easyretrofit.NetworkException
import com.dengzii.easyretrofit.interfaces.Logger
import com.dengzii.easyretrofit.interfaces.NetworkLogger
import com.dengzii.easyretrofit.interfaces.ResponseBodyInterceptor
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException
import java.util.*

class ResponseInterceptor(
    private val mLogger: NetworkLogger,
    private val mInterceptor: ResponseBodyInterceptor?
) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originResponse = chain.proceed(chain.request())
        val responseBuilder = originResponse.newBuilder()
        if (originResponse.isSuccessful && mInterceptor != null) {
            responseBuilder.body(handleSuccess(originResponse))
        } else {
//            handleFailed(originResponse);
        }
        return responseBuilder.build()
    }

    @Throws(IOException::class)
    private fun handleSuccess(originResponse: Response): ResponseBody? {
        val body = originResponse.body ?: return null
        if (hasResponseBody(originResponse) && (isJsonBody(body) || isTextBody(body))) {
            var json = body.string()
            logI(Logger.RESPONSE_BODY, json)
            if (mInterceptor != null) {
                json = mInterceptor.onIntercept(json)
            }
            return json.toResponseBody(TYPE_JSON)
        }
        return null
    }

    @Throws(IOException::class)
    private fun handleFailed(response: Response) {
        val exception = NetworkException(response.code, response.message)
        logE(Logger.RESPONSE_BODY, exception)
        throw exception
    }

    private fun isTextBody(body: ResponseBody): Boolean {
        return body.contentType()!!.type.toLowerCase(Locale.ROOT).startsWith("text")
    }

    private fun isJsonBody(body: ResponseBody?): Boolean {
        return try {
            body!!.contentType() == TYPE_JSON || body.contentType().toString()
                .contains("application/json")
        } catch (e: Exception) {
            logE(Logger.RESPONSE_TYPE, e)
            false
        }
    }

    private fun hasResponseBody(response: Response): Boolean {
        return response.body != null && response.body!!.contentType() != null
    }

    private fun logI(type: Int, log: String) {
        mLogger.i(type, TAG, log)
    }

    private fun logE(type: Int, e: Throwable) {
        mLogger.e(type, TAG, e)
    }

    companion object {
        private const val TAG = "ResponseBodyInterceptor"
        private val TYPE_JSON: MediaType = "application/json; charset=UTF-8".toMediaType()
    }

}
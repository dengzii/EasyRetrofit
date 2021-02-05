package com.dengzii.easyretrofit.interceptor

import com.dengzii.easyretrofit.CommonRequestBody
import com.dengzii.easyretrofit.NetworkException
import com.dengzii.easyretrofit.RetrofitManager
import com.dengzii.easyretrofit.interfaces.GlobalParamProvider
import com.dengzii.easyretrofit.interfaces.Logger
import com.dengzii.easyretrofit.interfaces.NetworkLogger
import com.google.gson.JsonObject
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okio.Buffer
import java.io.IOException

class GeneralParamInterceptor(
    private val mLogger: NetworkLogger,
    private val mParamProvider: GlobalParamProvider,
) : Interceptor {

    private val mGson by lazy { RetrofitManager.getInstance().gson }

    companion object {
        private const val TAG = "GeneralParamInterceptor"
        private val TYPE_JSON: MediaType = "application/json; charset=UTF-8".toMediaType()
        private val ComplexRequestTypes = arrayOf("POST", "DELETE", "PUT", "PATCH")
    }


    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {

        val request = chain.request()
        val requestBuilder = request.newBuilder()
        val originParameters = try {
            getRequestParameters(request)
        } catch (e: Throwable) {
            mLogger.e(Logger.GENERAL, TAG, e)
            throw NetworkException("Can not read request parameter.", e)
        }

        val globalParameters = mParamProvider.getBodyParam(
            request.method,
            request.url.toString(),
            originParameters
        )
        if (globalParameters.isNotEmpty()) {
            if (request.method in ComplexRequestTypes) {

                val nBody = if (request.body == null) {
                    CommonRequestBody.create(request.header("Content-Type"))
                } else {
                    CommonRequestBody.create(request.body!!)
                }
                val attachSuccess = nBody.addParams(globalParameters)
                if (!attachSuccess) {
                    mLogger.e(Logger.REQUEST_BODY, TAG,
                        Exception("failed to attach common parameters to ${request.url}"))
                }
                requestBuilder
                    .method(request.method, nBody)
            }
            if ("GET" == request.method) {
                requestBuilder
                    .method(request.method, request.body)
                    .url(attachToGet(request, globalParameters))
            }
        }
        val queryParam = mParamProvider.getQueryParam(request, originParameters)
        if (queryParam.isNotEmpty()) {
            val url = request.url
            val newUrlBuilder = url.newBuilder()
            queryParam.forEach {
                if (url.queryParameter(it.key).isNullOrBlank()) {
                    newUrlBuilder.addQueryParameter(it.key, it.value)
                }
            }
            requestBuilder.url(newUrlBuilder.build())
        }

        val headers = mParamProvider.getHeader(request, originParameters)
        if (headers.isNotEmpty()) {
            val hd = request.headers
            val hdBuilder = hd.newBuilder()
            headers.forEach {
                if (hd[it.key].isNullOrBlank()) {
                    hdBuilder.add(it.key, it.value)
                }
            }
            requestBuilder.headers(hdBuilder.build())
        }
        return chain.proceed(requestBuilder.build())
    }

    private fun getRequestParameters(originRequest: Request): Map<String, String> {
        var parameters: Map<String, String> = HashMap()
        if ("GET" == originRequest.method) {
            parameters = getGetParameters(originRequest)
        }
        if (originRequest.method in ComplexRequestTypes) {
            val requestBody = originRequest.body ?: return mapOf()
            if (requestBody is FormBody) {
                parameters = getFormBodyParameters(requestBody)
            }
            if (requestBody is MultipartBody) {
                parameters = getMultipartBodyParameters(requestBody)
            }
            if (TYPE_JSON == requestBody.contentType()) {
                parameters = getJsonParameters(requestBody)
            }
        }
        return parameters
    }

    private fun getJsonParameters(requestBody: RequestBody): Map<String, String> {
        val parameters = mutableMapOf<String, String>()
        val jsonObject = mGson.fromJson(
            bodyToString(requestBody),
            JsonObject::class.java
        )
        for (key in jsonObject.keySet()) {
            if (jsonObject[key].isJsonNull) {
                parameters[key] = ""
                continue
            }
            parameters[key] = jsonObject[key].asString
        }
        return parameters
    }

    private fun getMultipartBodyParameters(multipartBody: MultipartBody): Map<String, String> {
        val parameters = mutableMapOf<String, String>()
        for (i in 0 until multipartBody.size) {
            val part = multipartBody.part(i)
            if (part.body.contentType() === MultipartBody.FORM) {
                // TODO: 2019/8/9 add general parameters to multipart body
                mLogger.e(
                    Logger.GENERAL,
                    TAG,
                    Exception("NO GENERAL PARAMETER ADD TO MULTIPART BODY")
                )
            }
        }
        return parameters
    }

    private fun getGetParameters(originRequest: Request): Map<String, String> {
        val parameters: MutableMap<String, String> = HashMap()
        val httpUrl = originRequest.url
        for (key in httpUrl.queryParameterNames) {
            parameters[key] = httpUrl.queryParameter(key) ?: ""
        }
        return parameters
    }

    private fun getFormBodyParameters(formBody: FormBody): Map<String, String> {
        val parameters: MutableMap<String, String> = HashMap()
        for (i in 0 until formBody.size) {
            parameters[formBody.name(i)] = formBody.value(i)
        }
        return parameters
    }

    private fun attachToGet(originRequest: Request, params: Map<String, String>): HttpUrl {
        val urlBuilder = originRequest.url
            .newBuilder()
            .scheme(originRequest.url.scheme)
            .host(originRequest.url.host)
        for (key in params.keys) {
            urlBuilder.addQueryParameter(key, params[key])
        }
        return urlBuilder.build()
    }

    private fun bodyToString(request: RequestBody?): String {
        try {
            val buffer = Buffer()
            if (request != null) request.writeTo(buffer) else return ""
            return buffer.readUtf8()
        } catch (e: IOException) {
            mLogger.e(
                Logger.GENERAL,
                TAG,
                e
            )
        }
        return ""
    }
}
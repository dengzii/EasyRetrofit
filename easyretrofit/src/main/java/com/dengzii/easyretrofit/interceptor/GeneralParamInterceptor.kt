package com.dengzii.easyretrofit.interceptor

import com.dengzii.easyretrofit.JsonRequestBody
import com.dengzii.easyretrofit.NetworkException
import com.dengzii.easyretrofit.RetrofitManager
import com.dengzii.easyretrofit.interfaces.GlobalParamProvider
import com.dengzii.easyretrofit.interfaces.Logger
import com.dengzii.easyretrofit.interfaces.NetworkLogger
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okio.Buffer
import java.io.IOException

class GeneralParamInterceptor(
    private val mLogger: NetworkLogger,
    private val mParamProvider: GlobalParamProvider,
    private val mGson: Gson = RetrofitManager.getInstance().gson
) : Interceptor {

    companion object {
        private const val TAG = "GeneralParamInterceptor"
        private val TYPE_JSON: MediaType = "application/json; charset=UTF-8".toMediaType()
        private val ComplexRequestTypes = arrayOf("POST", "DELETE", "PUT")
    }


    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {

        var originRequest = chain.request()
        val originParameters = try {
            getRequestParameters(originRequest)
        } catch (e: Throwable) {
            mLogger.e(Logger.GENERAL, TAG, e)
            throw NetworkException("Can not read request parameter.", e)
        }

        val globalParameters = mParamProvider.getParam(
            originRequest.method,
            originRequest.url.toString(),
            originParameters
        )
        if (globalParameters.isNotEmpty()) {
            if (originRequest.method in ComplexRequestTypes) {
                val newBody = attachToBody(originRequest.body, globalParameters)
                originRequest = originRequest.newBuilder()
                    .post(newBody!!)
                    .build()
            }
            if ("GET" == originRequest.method) {
                originRequest = originRequest.newBuilder()
                    .method(originRequest.method, originRequest.body)
                    .url(attachToGet(originRequest, globalParameters))
                    .build()
            }
        }
        return chain.proceed(originRequest)
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
                //                log(part.body().contentType().toString());
//                log(part.body().toString());
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

    private fun attachToBody(originBody: RequestBody?, param: Map<String, String>): RequestBody? {
        var result: RequestBody? = null

        // for empty request body
        if (originBody == null) {
            val json = mGson
                .toJson(mParamProvider.getParam("POST", "", mapOf()))
            return JsonRequestBody.create(json)
        }

        // for form body, the general string request body
        when {
            originBody is FormBody -> {
                val formBody = createOrCloneFormBody(originBody as FormBody?, param)
                result = createRequestBody(
                    TYPE_JSON,
                    bodyToString(formBody)
                )
            }
            originBody is MultipartBody -> {
                log("Start attach global parameters to MultipartBody.")
                result = createOrCloneMultipartBody(originBody, param)
            }
            TYPE_JSON == originBody.contentType() -> {
                result = attachToJsonBody(originBody, param)
            }
            else -> {
                try {
                    return if (originBody.contentLength() == 0L) {
                        attachToBody(null, param)
                    } else {
                        val buffer = Buffer()
                        originBody.writeTo(buffer)
                        mLogger.e(
                            Logger.GENERAL,
                            TAG,
                            Exception(
                                "Unknown request body type. "
                                        + originBody.contentType().toString()
                            )
                        )
                        originBody
                    }
                } catch (e: IOException) {
                    mLogger.e(
                        Logger.GENERAL,
                        TAG,
                        e
                    )
                } catch (e: NullPointerException) {
                    mLogger.e(
                        Logger.GENERAL,
                        TAG,
                        e
                    )
                }
            }
        }
        return result
    }

    /**
     * 创建一个 FormBodyBuilder 并从现有请求参数复制参数
     *
     * @param originBody the origin request body
     * @return the new FormBody attached general parameter
     */
    private fun createOrCloneFormBody(originBody: FormBody?, param: Map<String, String>): FormBody {
        val formBodyBuilder = FormBody.Builder()
        for (key in param.keys) {
            formBodyBuilder.add(key, param.getOrElse(key) { "" })
        }
        if (originBody == null) {
            return formBodyBuilder.build()
        }
        val size = originBody.size
        for (i in 0 until size) {
            formBodyBuilder.add(originBody.name(i), originBody.value(i))
        }
        return formBodyBuilder.build()
    }

    private fun attachToJsonBody(
        requestBody: RequestBody,
        param: Map<String, String>
    ): RequestBody {
        try {
            val jsonObject = RetrofitManager.getInstance()
                .gson.fromJson(bodyToString(requestBody), JsonObject::class.java)
            for (key in param.keys) {
                jsonObject.add(key, JsonPrimitive(param[key]))
            }
            log(jsonObject.toString())
            return createRequestBody(
                TYPE_JSON,
                jsonObject.toString()
            )
        } catch (e: Exception) {
            mLogger.e(
                Logger.GENERAL,
                TAG,
                e
            )
        }
        return requestBody
    }

    private fun createOrCloneMultipartBody(
        originBody: MultipartBody,
        param: Map<String, String>
    ): MultipartBody {
        log("Start handle MultipartBody")
        val oldPartList = originBody.parts
        val newBodyBuilder = MultipartBody.Builder()
        newBodyBuilder.setType(MultipartBody.FORM)

//        RequestBody bodyPart;
        for (key in param.keys) {
//            bodyPart = RequestBody.create(universalParam.get(key), MediaType.parse("text/plain"));
            newBodyBuilder.addFormDataPart(key, param.getOrElse(key) { "" })
        }
        for (part in oldPartList) {
            newBodyBuilder.addPart(part)
        }
        //        newBodyBuilder.addPart(originBody);
        return newBodyBuilder.build()
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

    private fun createRequestBody(type: MediaType, body: String): RequestBody {
        return body.toRequestBody(type)
    }

    private fun log(log: String) {
        mLogger.i(
            Logger.GENERAL,
            TAG,
            log
        )
    }

}
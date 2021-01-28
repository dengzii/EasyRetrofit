package com.dengzii.easyretrofit

import okhttp3.FormBody
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.BufferedSink

@Suppress("MemberVisibilityCanBePrivate")
class CommonRequestBody private constructor() : RequestBody() {

    private lateinit var requestBody: RequestBody
    var contentType: MediaType? = null

    companion object {

        val TYPE_JSON = "application/json; charset=UTF-8".toMediaType()
        val TYPE_FORM = "application/x-www-form-urlencoded".toMediaType()
        val TYPE_MULTIPART = "multipart/form-data".toMediaType()
        val TYPE_BINARY = "application/octet-stream".toMediaType()

        fun create(medeaType: String?): CommonRequestBody {
            return CommonRequestBody().apply {
                contentType = medeaType?.toMediaType()
                createBody(contentType)?.let {
                    requestBody = it
                }
            }
        }

        fun create(contentType: MediaType): CommonRequestBody {
            return CommonRequestBody().apply {
                createBody(contentType)?.let {
                    requestBody = it
                }
                this.contentType = contentType
            }
        }

        fun create(origin: RequestBody): CommonRequestBody {
            return CommonRequestBody().apply {
                requestBody = origin
                contentType = origin.contentType()
            }
        }

        private fun createBody(contentType: MediaType?): RequestBody? {
            return when (contentType) {
                TYPE_JSON -> JsonRequestBody.create()
                TYPE_FORM -> FormBody.Builder().build()
                TYPE_MULTIPART -> MultipartBody.Builder().build()
                // TYPE_BINARY -> MultipartBody.Builder().build()
                else -> null
            }
        }
    }

    fun addParams(params: Map<String, String>) :Boolean{
        when (requestBody) {
            is MultipartBody -> {
                val b = requestBody as MultipartBody
                val builder = MultipartBody.Builder()
                    .setType(b.type)
                b.parts.forEach {
                    builder.addPart(it)
                }
                params.forEach {
                    builder.addFormDataPart(it.key, it.value)
                }
                requestBody = builder.build()
            }
            is JsonRequestBody -> {
                (requestBody as? JsonRequestBody)?.add(params)
            }
            is FormBody -> {
                val r = (requestBody as FormBody)
                val builder = FormBody.Builder()
                for (i in 0 until r.size) {
                    builder.addEncoded(r.encodedName(i), r.encodedValue(i))
                }
                params.forEach {
                    builder.add(it.key, it.value)
                }
                requestBody = builder.build()
            }
            // ContentTypeOverrideBody
            else -> {
                return false
            }
        }
        return true
    }

    override fun isDuplex(): Boolean {
        return getBody()?.isDuplex() ?: super.isDuplex()
    }

    override fun isOneShot(): Boolean {
        return getBody()?.isOneShot() ?: super.isOneShot()
    }

    override fun contentLength(): Long {
        return getBody()?.contentLength() ?: -1
    }

    override fun contentType(): MediaType? {
        return contentType
    }

    override fun writeTo(sink: BufferedSink) {
        getBody()?.writeTo(sink)
        sink.flush()
    }

    private fun getBody(): RequestBody? {
        return if (this::requestBody.isInitialized) requestBody else null
    }
}
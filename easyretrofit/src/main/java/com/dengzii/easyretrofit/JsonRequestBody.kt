package com.dengzii.easyretrofit

import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.internal.checkOffsetAndCount
import okio.BufferedSink
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.Charset

class JsonRequestBody private constructor(private val mJsonBody: String) : RequestBody() {

    private lateinit var json: JSONObject

    companion object {
        private val JSON_TYPE: MediaType = "application/json; charset=utf-8".toMediaType()
        private val CHARSET_UTF8 =
            Charset.forName("utf-8")

        fun create(json: String): JsonRequestBody {
            return JsonRequestBody(json)
        }

        fun create(): JsonRequestBody {
            return JsonRequestBody("{}")
        }

        fun create(map: Map<String, Any>): JsonRequestBody {
            return create(RetrofitManager.getInstance().gson.toJson(map))
        }
    }

    override fun contentType(): MediaType {
        return JSON_TYPE
    }

    /**
     *  @param value a JSONObject, JSONArray, String, Boolean, Integer, Long, Double,
     *              NULL, or null. May not be NaNs or infinities.
     */
    fun add(key: String, value: Any) {
        if (!this::json.isInitialized) {
            json = JSONObject(mJsonBody)
        }
        json.put(key, value)
    }

    fun add(map: Map<String, Any>) {
        if (!this::json.isInitialized) {
            json = JSONObject(mJsonBody)
        }
        map.forEach {
            json.put(it.key, it.value)
        }
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        val bytes = if (this::json.isInitialized) {
            json.toString().toByteArray(CHARSET_UTF8)
        } else {
            mJsonBody.toByteArray(CHARSET_UTF8)
        }
        checkOffsetAndCount(bytes.size.toLong(), 0, bytes.size.toLong())
        sink.write(bytes, 0, bytes.size)
    }
}
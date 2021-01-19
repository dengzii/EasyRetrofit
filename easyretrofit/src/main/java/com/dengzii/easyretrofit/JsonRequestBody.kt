package com.dengzii.easyretrofit

import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.internal.checkOffsetAndCount
import okio.BufferedSink
import java.io.IOException
import java.nio.charset.Charset

class JsonRequestBody private constructor(private val mJsonBody: String) : RequestBody() {

    override fun contentType(): MediaType? {
        return JSON_TYPE
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        val bytes = mJsonBody.toByteArray(CHARSET_UTF8)
        checkOffsetAndCount(bytes.size.toLong(), 0, bytes.size.toLong())
        sink.write(bytes, 0, bytes.size)
    }

    companion object {
        private val JSON_TYPE: MediaType = "application/json; charset=utf-8".toMediaType()
        private val CHARSET_UTF8 =
            Charset.forName("utf-8")

        fun create(json: String): JsonRequestBody {
            return JsonRequestBody(json)
        }
    }

}
package com.dengzii.easyretrofit.interfaces

interface Logger {

    fun i(tag: String, log: String)
    fun e(tag: String, e: Throwable)

    companion object {
        const val ALL = -0x1
        const val NONE = 0
        const val REQUEST_URL = 1
        const val RESPONSE_HEADERS = 1 shl 1
        const val REQUEST_HEADERS = 1 shl 2
        const val REQUEST_BODY = 1 shl 3
        const val RESPONSE_BODY = 1 shl 4
        const val RESPONSE_TYPE = 1 shl 5
        const val REQUEST_TYPE = 1 shl 6
        const val BEAN_MAPPING = 1 shl 7
        const val GENERAL = 1 shl 8
    }
}
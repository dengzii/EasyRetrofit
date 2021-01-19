package com.dengzii.easyretrofit.interfaces

interface NetworkLogger {
    fun i(type: Int, tag: String, log: String)
    fun e(type: Int, tag: String, log: Throwable)
    val level: Int
    fun isLogType(type: Int): Boolean
}
package com.dengzii.easyretrofit.interfaces

import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.io.File

interface RetrofitManagerBuilder {

    fun setOkHttpClient(client: OkHttpClient):RetrofitManagerBuilder
    fun gson(gson: Gson): RetrofitManagerBuilder
    fun baseUrl(url: String): RetrofitManagerBuilder
    fun connectionTimeout(millSecond: Long): RetrofitManagerBuilder
    fun readTimeout(millSecond: Long): RetrofitManagerBuilder
    fun writeTimeout(millSecond: Long): RetrofitManagerBuilder
    fun verifiedHost(hostname: String): RetrofitManagerBuilder
    fun logger(logger: Logger?): RetrofitManagerBuilder
    fun logLevel(logType: Int): RetrofitManagerBuilder
    fun disableLog(logType: Int): RetrofitManagerBuilder
    fun addInterceptor(interceptor: Interceptor): RetrofitManagerBuilder
    fun responseInterceptor(interceptor: ResponseBodyInterceptor): RetrofitManagerBuilder
    fun globalParamProvider(paramProvider: GlobalParamProvider): RetrofitManagerBuilder
    fun cache(file: File, size: Long): RetrofitManagerBuilder
    fun build()
}
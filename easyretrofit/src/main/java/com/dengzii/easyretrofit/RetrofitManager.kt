package com.dengzii.easyretrofit

import com.dengzii.easyretrofit.converter.GsonConverterFactory
import com.dengzii.easyretrofit.interceptor.GeneralParamInterceptor
import com.dengzii.easyretrofit.interceptor.LogInterceptor
import com.dengzii.easyretrofit.interceptor.ResponseInterceptor
import com.dengzii.easyretrofit.interfaces.GlobalParamProvider
import com.dengzii.easyretrofit.interfaces.Logger
import com.dengzii.easyretrofit.interfaces.ResponseBodyInterceptor
import com.dengzii.easyretrofit.interfaces.RetrofitManagerBuilder
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSession

class RetrofitManager private constructor() {

    private lateinit var mRetrofit: Retrofit
    internal lateinit var gson: Gson

    companion object {

        private lateinit var sInstance: RetrofitManager

        fun getInstance(): RetrofitManager {
            checkIsInitialize()
            return sInstance
        }

        fun getGson(): Gson {
            checkIsInitialize()
            return sInstance.gson
        }

        fun <T> create(c: Class<T>): T {
            return getInstance().mRetrofit.create(c)
        }

        fun newBuilder(): RetrofitManagerBuilder {
            return Builder()
        }

        private fun checkIsInitialize() {
            if (!this::sInstance::isInitialized.get()) {
                throw RuntimeException("RetrofitManager does not init, please init it first.")
            }
        }
    }

    internal class Builder : RetrofitManagerBuilder {

        private var mOkHttpBuilder = OkHttpClient.Builder()
        private val mRetrofitBuilder = Retrofit.Builder()
        private val mHostnameVerifier = MyHostnameVerifier()

        private lateinit var mGson: Gson
        private val mLoggerManager = LoggerManager()

        override fun setOkHttpClient(client: OkHttpClient) = apply {
            mOkHttpBuilder = client.newBuilder()
        }

        override fun baseUrl(url: String) = apply { mRetrofitBuilder.baseUrl(url) }

        override fun verifiedHost(hostname: String) = apply { mHostnameVerifier.addHost(hostname) }

        override fun logger(logger: Logger?) = apply { mLoggerManager.logger = logger }

        override fun gson(gson: Gson) = apply { mGson = gson }

        override fun logLevel(logType: Int) = apply { mLoggerManager.level and logType }

        override fun disableLog(logType: Int) = apply { mLoggerManager.level xor logType }

        override fun addInterceptor(interceptor: Interceptor) = apply {
            mOkHttpBuilder.addInterceptor(interceptor)
        }

        override fun responseInterceptor(interceptor: ResponseBodyInterceptor) = apply {
            mOkHttpBuilder.addInterceptor(ResponseInterceptor(mLoggerManager, interceptor))
        }

        override fun globalParamProvider(paramProvider: GlobalParamProvider) = apply {
            mOkHttpBuilder.addInterceptor(GeneralParamInterceptor(mLoggerManager, paramProvider))
        }

        override fun cache(file: File, size: Long) = apply {
            mOkHttpBuilder.cache(Cache(file, size))
        }

        override fun connectionTimeout(millSecond: Long) = apply {
            mOkHttpBuilder.connectTimeout(millSecond, TimeUnit.MILLISECONDS)
        }

        override fun readTimeout(millSecond: Long) = apply {
            mOkHttpBuilder.readTimeout(millSecond, TimeUnit.MILLISECONDS)
        }

        override fun writeTimeout(millSecond: Long) = apply {
            mOkHttpBuilder.writeTimeout(millSecond, TimeUnit.MILLISECONDS)
        }

        override fun build() {

            if (mLoggerManager.logger != null) {
                mOkHttpBuilder.addInterceptor(LogInterceptor(mLoggerManager))
            }

            val retrofitManager = RetrofitManager()
            mRetrofitBuilder.validateEagerly(true)

            if (!::mGson::isInitialized.get()) {
                retrofitManager.gson = GsonBuilder()
                    .serializeNulls()
                    .setLenient()
                    .create()
            }

            mRetrofitBuilder
                .addConverterFactory(GsonConverterFactory.create(mGson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(mOkHttpBuilder.build())

            retrofitManager.gson = mGson
            retrofitManager.mRetrofit = mRetrofitBuilder.build()

            sInstance = retrofitManager
        }
    }

    private class MyHostnameVerifier : HostnameVerifier {

        private val mVerifiedHost = mutableListOf<String>()

        fun addHost(hostname: String) {
            mVerifiedHost.add(hostname)
        }

        override fun verify(hostname: String, session: SSLSession): Boolean {
            for (host in mVerifiedHost) {
                if (hostname.contains(host)) {
                    return true
                }
            }
            return false
        }
    }

}
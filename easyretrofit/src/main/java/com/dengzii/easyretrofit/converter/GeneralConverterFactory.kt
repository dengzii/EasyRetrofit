package com.dengzii.easyretrofit.converter

import com.dengzii.easyretrofit.interfaces.Logger
import com.dengzii.easyretrofit.interfaces.NetworkLogger
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

class GeneralConverterFactory : Converter.Factory {
    private var mLogger: NetworkLogger? = null

    private constructor() {}

    private constructor(logger: NetworkLogger) {
        mLogger = logger
    }

    override fun requestBodyConverter(
        type: Type, parameterAnnotations: Array<Annotation>,
        methodAnnotations: Array<Annotation>, retrofit: Retrofit
    ): Converter<*, RequestBody>? {
        log("Converter Request Body, Type : $type")
        return super.requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit)
    }

    override fun stringConverter(
        type: Type, annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<*, String>? {
        return super.stringConverter(type, annotations, retrofit)
    }

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *>? {
        log("Converter Response Body, Type : $type")
        return super.responseBodyConverter(type, annotations, retrofit)
    }

    private fun log(msg: String) {
        mLogger!!.i(
            Logger.BEAN_MAPPING,
            TAG,
            msg
        )
    }

    companion object {
        private const val TAG = "GeneralConverterFactory"
        private var INSTANCE: GeneralConverterFactory? = null
        @JvmStatic
        fun create(logger: NetworkLogger): GeneralConverterFactory? {
            if (INSTANCE == null) {
                synchronized(GeneralConverterFactory::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = GeneralConverterFactory(logger)
                    }
                }
            }
            return INSTANCE
        }
    }
}
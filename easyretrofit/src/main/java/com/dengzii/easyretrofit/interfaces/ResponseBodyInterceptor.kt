package com.dengzii.easyretrofit.interfaces

interface ResponseBodyInterceptor {
    fun onIntercept(originResponse: String): String
}
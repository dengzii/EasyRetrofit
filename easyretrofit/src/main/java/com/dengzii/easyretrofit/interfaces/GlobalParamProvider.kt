package com.dengzii.easyretrofit.interfaces

interface GlobalParamProvider {
    fun getParam(
        method: String,
        url: String,
        originParameters: Map<String, String>
    ): Map<String, String>
}
package com.dengzii.easyretrofit.interfaces

import okhttp3.Request

open class GlobalParamProvider {

    open fun getHeader(
        request: Request,
        originParameters: Map<String, String>
    ): Map<String, String> {
        return emptyMap()
    }

    open fun getBodyParam(
        method: String,
        url: String,
        originParameters: Map<String, String>
    ): Map<String, String> {
        return emptyMap()
    }

    open fun getQueryParam(
        request: Request,
        originParameters: Map<String, String>
    ): Map<String, String> {
        return emptyMap()
    }
}
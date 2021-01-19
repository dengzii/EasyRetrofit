package com.dengzii.easyretrofit

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

abstract class CookieJar : CookieJar {

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return listOf()
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {

    }
}
package com.dengzii.easyretrofit

import java.io.IOException


class NetworkException : IOException {

    var code = -1

    constructor(message: String, cause: Throwable) : super(message, cause)

    constructor(code: Int, message: String) : super("$code, $message") {
        this.code = code
    }
}
package com.dengzii.easyretrofit

import com.dengzii.easyretrofit.interfaces.Logger
import com.dengzii.easyretrofit.interfaces.NetworkLogger

internal class LoggerManager(
    var logger: Logger? = null,
    override val level: Int = Logger.ALL
) : NetworkLogger {

    override fun i(type: Int, tag: String, log: String) {
        if (isLogType(type)) {
            logger?.i("I/$TAG $tag", log)
        }
    }

    override fun e(type: Int, tag: String, log: Throwable) {
        if (isLogType(type)) {
            logger?.e("E/$TAG $tag", log)
        }
    }

    override fun isLogType(type: Int): Boolean {
        return level and type != 0
    }

    companion object {
        private const val TAG = "NetWork"
    }

}
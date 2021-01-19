package com.dengzii.easyretrofit.adapter

import com.dengzii.easyretrofit.interfaces.Logger
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class IntDeserializer(private val mLogger: Logger) :
    JsonDeserializer<Int> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement, typeOfT: Type,
        context: JsonDeserializationContext
    ): Int {
        return if (json.asString.trim { it <= ' ' } == "") {
            0
        } else try {
            json.asInt
        } catch (e: NumberFormatException) {
            mLogger.e(TAG, e)
            0
        }
    }

    companion object {
        private val TAG = IntDeserializer::class.java.simpleName
    }

}
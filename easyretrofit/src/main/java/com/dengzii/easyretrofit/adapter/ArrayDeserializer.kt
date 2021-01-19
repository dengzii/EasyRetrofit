package com.dengzii.easyretrofit.adapter

import com.dengzii.easyretrofit.RetrofitManager
import com.dengzii.easyretrofit.interfaces.Logger
import com.google.gson.*
import java.lang.reflect.Type

class ArrayDeserializer(private val mLogger: Logger) :

    JsonDeserializer<List<*>> {
    private val mGson: Gson

    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement, typeOfT: Type,
        context: JsonDeserializationContext
    ): List<*> {
        return if (json.isJsonArray) {
            RetrofitManager.getInstance().gson
                .fromJson(json, typeOfT)
        } else {
            emptyList<Any>()
        }
    }

    companion object {
        private val TAG = ArrayDeserializer::class.java.simpleName
    }

    init {
        val origin = RetrofitManager.getInstance().gson
        mGson = origin.newBuilder()
            .registerTypeHierarchyAdapter(MutableList::class.java, null)
            .create()
    }
}
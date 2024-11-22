package zzz.me.yang.dev.ext.moshi.base

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import java.lang.reflect.Type

public abstract class SingleJsonAdapter<T> : JsonAdapter<T>() {
    public abstract class Factory(private val clz: Class<*>) : JsonAdapter.Factory {
        final override fun create(
            type: Type,
            annotations: MutableSet<out Annotation>,
            moshi: Moshi,
        ): JsonAdapter<*>? {
            if (type != clz) return null

            return createAdapter(type, annotations, moshi)
        }

        protected abstract fun createAdapter(
            type: Type,
            annotations: MutableSet<out Annotation>,
            moshi: Moshi,
        ): JsonAdapter<*>?
    }

    final override fun fromJson(jsonReader: JsonReader): T? {
        return from(jsonReader)
    }

    protected abstract fun from(reader: JsonReader): T?

    final override fun toJson(jsonWriter: JsonWriter, value: T?) {
        to(jsonWriter, value)
    }

    protected abstract fun to(writer: JsonWriter, value: T?)
}

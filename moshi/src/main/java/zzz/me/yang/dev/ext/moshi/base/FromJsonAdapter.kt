package zzz.me.yang.dev.ext.moshi.base

import com.squareup.moshi.JsonWriter

internal abstract class FromJsonAdapter<T> : SingleJsonAdapter<T>() {
    final override fun toJson(writer: JsonWriter, value: T?) {
        throw UnsupportedOperationException("This adapter is only used to deserialize objects")
    }
}

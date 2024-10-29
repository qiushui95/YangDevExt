package zzz.me.yang.dev.ext.moshi.base

import com.squareup.moshi.JsonReader

internal abstract class ToJsonAdapter<T> : SingleJsonAdapter<T>() {
    final override fun fromJson(reader: JsonReader): T? {
        throw UnsupportedOperationException("This adapter is only used to serialize objects")
    }
}

package zzz.me.yang.dev.ext.moshi.base

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import java.lang.reflect.Type

internal abstract class SingleJsonAdapter<T> : JsonAdapter<T>() {
    abstract class Factory(private val clz: Class<*>) : JsonAdapter.Factory {
        final override fun create(
            type: Type,
            annotations: MutableSet<out Annotation>,
            moshi: Moshi,
        ): JsonAdapter<*>? {
            if (type != clz) return null

            return create(annotations, moshi)
        }

        protected abstract fun create(
            annotations: MutableSet<out Annotation>,
            moshi: Moshi,
        ): JsonAdapter<*>?
    }
}

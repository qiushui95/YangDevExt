package zzz.me.yang.dev.ext.moshi.factory

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import zzz.me.yang.dev.ext.moshi.anno.StrHandler
import zzz.me.yang.dev.ext.moshi.base.FromJsonAdapter
import zzz.me.yang.dev.ext.moshi.base.SingleJsonAdapter

internal class StrHandlerFactory : SingleJsonAdapter.Factory(String::class.java) {
    private class Adapter(
        private val handler: StrHandler,
        annotations: Set<Annotation>,
        moshi: Moshi,
    ) : FromJsonAdapter<String>() {
        private val stringJsonAdapter = moshi.adapter<String>(String::class.java, annotations)

        override fun fromJson(reader: JsonReader): String? {
            val value = stringJsonAdapter.fromJson(reader) ?: return null

            if (value in handler.nullList) return null

            if (value.isBlank() && handler.blank2Null) return null

            return value.handleNewLine().handleTrim().handleMaxLength()
        }

        private fun String.handleNewLine(): String {
            if (handler.replaceNewLine.not()) return this

            return replace("\\n", "\n")
        }

        private fun String.handleTrim(): String {
            if (handler.trim.not()) return this

            return trim()
        }

        private fun String.handleMaxLength(): String {
            if (handler.maxLength <= 0) return this

            return take(handler.maxLength)
        }
    }

    override fun create(
        annotations: MutableSet<out Annotation>,
        moshi: Moshi,
    ): JsonAdapter<*>? {
        val handler = annotations.filterIsInstance<StrHandler>()
            .firstOrNull()
            ?: return null

        val otherAnnotations = annotations.filterNot { it is StrHandler }

        return Adapter(handler, otherAnnotations.toSet(), moshi)
    }
}

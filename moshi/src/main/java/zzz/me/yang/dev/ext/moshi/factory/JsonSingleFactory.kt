package zzz.me.yang.dev.ext.moshi.factory

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import zzz.me.yang.dev.ext.moshi.anno.JsonSingle
import zzz.me.yang.dev.ext.moshi.base.SingleJsonAdapter
import java.lang.reflect.Type

internal class JsonSingleFactory : JsonAdapter.Factory {
    private class Adapter(
        moshi: Moshi,
        type: Type,
        annotations: MutableSet<out Annotation>,
        private val jsonSingle: JsonSingle,
    ) : SingleJsonAdapter<Any>() {
        private val adapter = moshi.adapter<Any>(type, annotations)
        private val options = JsonReader.Options.of(jsonSingle.value)

        override fun from(reader: JsonReader): Any? {
            reader.beginObject()

            var result: Any? = null

            while (reader.hasNext()) {
                when (reader.selectName(options)) {
                    0 -> result = adapter.fromJson(reader)

                    else -> {
                        reader.skipName()
                        reader.skipValue()
                    }
                }
            }

            reader.endObject()

            return result
        }

        override fun to(writer: JsonWriter, value: Any?) {
            writer.beginObject()
            writer.name(jsonSingle.value)
            adapter.toJson(writer, value)
            writer.endObject()
        }
    }

    override fun create(
        type: Type,
        annotations: MutableSet<out Annotation>,
        moshi: Moshi,
    ): JsonAdapter<*>? {
        val otherAnnotations = annotations.filterNot { it is JsonSingle }

        val singleAnnotation = annotations.filterIsInstance<JsonSingle>()
            .firstOrNull()
            ?: return null

        return Adapter(moshi, type, otherAnnotations.toMutableSet(), singleAnnotation)
    }
}

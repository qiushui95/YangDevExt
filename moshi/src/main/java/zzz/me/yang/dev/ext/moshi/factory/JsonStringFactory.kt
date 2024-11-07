package zzz.me.yang.dev.ext.moshi.factory

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.nextAnnotations
import okio.BufferedSource
import zzz.me.yang.dev.ext.moshi.anno.JsonString
import zzz.me.yang.dev.ext.moshi.base.SingleJsonAdapter
import java.lang.reflect.Type

internal class JsonStringFactory : JsonAdapter.Factory {
    private class Adapter : SingleJsonAdapter<String>() {
        override fun from(reader: JsonReader): String {
            return reader.nextSource().use(BufferedSource::readUtf8)
        }

        override fun to(writer: JsonWriter, value: String?) {
            writer.valueSink().use { sink -> sink.writeUtf8(checkNotNull(value)) }
        }
    }

    override fun create(
        type: Type,
        annotations: Set<Annotation>,
        moshi: Moshi,
    ): JsonAdapter<*>? {
        if (type != String::class.java) return null
        annotations.nextAnnotations<JsonString>() ?: return null
        return Adapter().nullSafe()
    }
}

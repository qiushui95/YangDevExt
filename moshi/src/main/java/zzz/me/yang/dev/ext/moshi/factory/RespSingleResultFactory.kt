package zzz.me.yang.dev.ext.moshi.factory

import RespSingleResult
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import zzz.me.yang.dev.ext.moshi.base.FromJsonAdapter
import java.lang.reflect.Type

internal class RespSingleResultFactory : JsonAdapter.Factory {
    private class Adapter(
        moshi: Moshi,
        type: Type,
        annotations: MutableSet<out Annotation>,
        singleParam: RespSingleResult,
    ) : FromJsonAdapter<Any>() {
        private val options = JsonReader.Options.of(singleParam.jsonName)
        private val jsonAdapter = moshi.adapter<Any>(type, annotations)

        override fun fromJson(jsonReader: JsonReader): Any? {
            jsonReader.beginObject()

            var result: Any? = null

            while (jsonReader.hasNext()) {
                when (jsonReader.selectName(options)) {
                    0 -> {
                        result = jsonAdapter.fromJson(jsonReader)
                    }

                    else -> {
                        jsonReader.skipName()
                        jsonReader.skipValue()
                    }
                }
            }

            jsonReader.endObject()

            return result
        }
    }

    override fun create(
        type: Type,
        annotations: MutableSet<out Annotation>,
        moshi: Moshi,
    ): JsonAdapter<*>? {
        val otherAnnotations = annotations.filterNot { it is RespSingleResult }

        val singleAnnotation = annotations.filterIsInstance<RespSingleResult>()
            .firstOrNull()
            ?: return null

        return Adapter(moshi, type, otherAnnotations.toMutableSet(), singleAnnotation)
    }
}

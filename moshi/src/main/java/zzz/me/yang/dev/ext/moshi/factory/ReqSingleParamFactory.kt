package zzz.me.yang.dev.ext.moshi.factory

import ReqSingleParam
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import zzz.me.yang.dev.ext.moshi.base.ToJsonAdapter
import java.lang.reflect.Type

internal class ReqSingleParamFactory : JsonAdapter.Factory {
    private class Adapter(
        private val moshi: Moshi,
        private val type: Type,
        private val annotations: MutableSet<out Annotation>,
        private val singleParam: ReqSingleParam,
    ) : ToJsonAdapter<Any>() {
        override fun toJson(jsonWriter: JsonWriter, value: Any?) {
            jsonWriter.beginObject()
            jsonWriter.name(singleParam.jsonName)
            moshi.adapter<Any>(type, annotations).toJson(jsonWriter, value)
            jsonWriter.endObject()
        }
    }

    override fun create(
        type: Type,
        annotations: MutableSet<out Annotation>,
        moshi: Moshi,
    ): JsonAdapter<*>? {
        val otherAnnotations = annotations.filterNot { it is ReqSingleParam }

        val singleAnnotation = annotations.filterIsInstance<ReqSingleParam>()
            .firstOrNull()
            ?: return null

        return Adapter(moshi, type, otherAnnotations.toMutableSet(), singleAnnotation)
    }
}

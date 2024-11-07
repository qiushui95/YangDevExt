package zzz.me.yang.dev.ext.moshi.base

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.internal.Util

public abstract class FromJsonAliasAdapter<T, P : FromJsonAliasParams> : FromJsonAdapter<T>() {
    public fun interface Handler<P : FromJsonAliasParams> {
        public fun fromJson(reader: JsonReader, params: P)
    }

    public class KeyListAndHandler<P : FromJsonAliasParams>(
        public val keyArray: Array<String>,
        public val handler: Handler<P>,
    )

    protected abstract val keyListAndHandlerArray: Array<KeyListAndHandler<P>>

    private val options: JsonReader.Options by lazy {
        createOptions()
    }

    private val keyEndIndexArray by lazy {
        createKeyEndIndexArray()
    }

    private val paramsMap: MutableMap<Int, P> = mutableMapOf()

    private fun createOptions(): JsonReader.Options {
        val list = keyListAndHandlerArray.flatMap { it.keyArray.toList() }

        val array = Array(list.size) { list[it] }

        return JsonReader.Options.of(*array)
    }

    private fun createKeyEndIndexArray(): Array<Int> {
        val list = mutableListOf<Int>()

        var index = 0

        for (array in keyListAndHandlerArray) {
            index += array.keyArray.size

            list.add(index)
        }

        return Array(list.size) { list[it] }
    }

    protected abstract fun createObject(reader: JsonReader, params: P): T?

    private fun keyString(keyArray: Array<String>): String {
        return keyArray.joinToString(",", prefix = "[", postfix = "]")
    }

    private fun getParams(jsonReader: JsonReader) = synchronized(paramsMap) {
        val key = jsonReader.hashCode()

        paramsMap.getOrPut(key) { createParams() }
    }

    private fun removeParams(jsonReader: JsonReader) = synchronized(paramsMap) {
        val key = jsonReader.hashCode()

        paramsMap.remove(key)
    }

    protected abstract fun createParams(): P

    final override fun from(reader: JsonReader): T? {
        val params = getParams(reader)

        when (reader.peek()) {
            JsonReader.Token.NULL -> {
                reader.nextNull<T>()
                return null
            }

            JsonReader.Token.BEGIN_OBJECT -> reader.beginObject()
            else -> throw JsonDataException(
                "Expected BEGIN_OBJECT or null but was ${reader.peek()}",
            )
        }

        while (reader.hasNext()) {
            val nameIndex = reader.selectName(options)

            if (nameIndex == -1) {
                reader.skipName()
                reader.skipValue()
                continue
            }

            for (indexValue in keyEndIndexArray.withIndex()) {
                val endIndex = indexValue.value

                if (nameIndex >= endIndex) continue

                keyListAndHandlerArray[indexValue.index].handler.fromJson(reader, params)

                break
            }
        }

        when (reader.peek()) {
            JsonReader.Token.END_OBJECT -> reader.endObject()
            else -> throw JsonDataException("Expected END_OBJECT but was ${reader.peek()}")
        }

        return createObject(reader, params).apply { removeParams(reader) }
    }

    protected fun unexpected(
        propertyName: String,
        keyArray: Array<String>,
        reader: JsonReader,
    ): Exception {
        return Util.unexpectedNull(propertyName, keyString(keyArray), reader)
    }

    protected fun missing(
        propertyName: String,
        keyArray: Array<String>,
        reader: JsonReader,
    ): Exception {
        return Util.missingProperty(propertyName, keyString(keyArray), reader)
    }
}

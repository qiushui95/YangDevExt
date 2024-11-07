package zzz.me.yang.dev.ext.moshi

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import zzz.me.yang.dev.ext.moshi.factory.JsonSingleFactory
import zzz.me.yang.dev.ext.moshi.factory.JsonStrHandlerFactory
import zzz.me.yang.dev.ext.moshi.factory.JsonStringFactory

public object MoshiFactories {
    public fun createAllFactory(): Array<JsonAdapter.Factory> {
        return arrayOf(
            JsonStringFactory(),
            JsonSingleFactory(),
            JsonStrHandlerFactory(),
        )
    }

    public fun Moshi.Builder.addAllFactory(): Moshi.Builder {
        for (factory in createAllFactory()) {
            add(factory)
        }

        return this
    }
}

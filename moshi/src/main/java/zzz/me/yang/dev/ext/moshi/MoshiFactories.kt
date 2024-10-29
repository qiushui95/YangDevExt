package zzz.me.yang.dev.ext.moshi

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import zzz.me.yang.dev.ext.moshi.factory.JsonStringFactory
import zzz.me.yang.dev.ext.moshi.factory.ReqSingleParamFactory
import zzz.me.yang.dev.ext.moshi.factory.StrHandlerFactory

public object MoshiFactories {
    public fun createAllFactory(): Array<JsonAdapter.Factory> {
        return arrayOf(
            JsonStringFactory(),
            ReqSingleParamFactory(),
            ReqSingleParamFactory(),
            StrHandlerFactory(),
        )
    }

    public fun Moshi.Builder.addAllFactory(): Moshi.Builder {
        for (factory in createAllFactory()) {
            add(factory)
        }

        return this
    }
}

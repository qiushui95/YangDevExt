package zzz.me.yang.dev.ext.coroutines

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

public typealias FlowList<T> = Flow<List<T>>

public fun <T, R> Flow<List<T>>.mapList(mapper: T.() -> R): Flow<List<R>> {
    return map { list ->
        list.map(mapper)
    }
}

public fun <T> Flow<List<T>>.filterList(filter: T.() -> Boolean): Flow<List<T>> {
    return map { list ->
        list.filter(filter)
    }
}

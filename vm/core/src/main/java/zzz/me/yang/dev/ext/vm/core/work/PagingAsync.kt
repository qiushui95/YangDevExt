package zzz.me.yang.dev.ext.vm.core.work

public sealed class PagingAsync(internal val sort: Int) {
    public open val isFullPage: Boolean = true
    public open val isSuccess: Boolean = false
    public open val isEmpty: Boolean = false

    public data object Uninitialized : PagingAsync(sort = 0)

    public data class Loading(override val isFullPage: Boolean) : PagingAsync(sort = 1)

    public data class Fail(
        override val isFullPage: Boolean,
        val error: Throwable,
    ) : PagingAsync(sort = 2)

    public data object EmptyPage : PagingAsync(sort = 3) {
        override val isSuccess: Boolean = true
        override val isEmpty: Boolean = true
    }

    public data class Success(val hasMore: Boolean) : PagingAsync(sort = 4) {
        override val isFullPage: Boolean = false
        override val isSuccess: Boolean = true
    }

    public operator fun plus(async: WorkAsync): PagingAsync {
        return if (async.sort < sort) {
            when (async) {
                WorkAsync.Uninitialized -> Uninitialized
                WorkAsync.Loading -> Loading(isFullPage)
                is WorkAsync.Fail -> Fail(isFullPage, async.error)
                WorkAsync.Success -> this
            }
        } else if (async == WorkAsync.Success && this is EmptyPage) {
            Success(hasMore = false)
        } else {
            this
        }
    }
}

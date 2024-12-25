package zzz.me.yang.dev.ext.vm.core.paging

import zzz.me.yang.dev.ext.entity.paging.PagingItem
import zzz.me.yang.dev.ext.entity.paging.PagingParam
import zzz.me.yang.dev.ext.entity.paging.PagingResponse
import zzz.me.yang.dev.ext.vm.core.work.PagingAsync
import zzz.me.yang.dev.ext.vm.core.work.WorkAsync

public data class PagingData<T : PagingItem>(
    val pageSize: Int = 20,
    val isRefresh: Boolean = true,
    val nextPage: Int = PagingResponse.pageStart,
    val emptyPage: Boolean = true,
    val hasMore: Boolean = true,
    val list: List<T> = emptyList(),
    val async: WorkAsync = WorkAsync.Uninitialized,
    val totalSize: Int = 0,
    val totalPage: Int = 0,
) {
    val isRefreshing: Boolean by lazy { isRefresh && async is WorkAsync.Loading }

    val needLoadInit: Boolean by lazy { async.shouldLoad && list.isEmpty() }

    val pagingAsync: PagingAsync by lazy {
        createPagingAsync()
    }

    public operator fun plus(itemInfo: T): PagingData<T> {
        val newList = list + itemInfo

        return copy(list = newList)
    }

    public operator fun minus(itemInfo: T): PagingData<T> {
        val newList = list.filterNot { it == itemInfo }

        return copy(list = newList)
    }

    private fun createPagingAsync(): PagingAsync {
        val isFullPage = list.isEmpty()

        return when {
            async is WorkAsync.Loading -> PagingAsync.Loading(isFullPage)
            async is WorkAsync.Fail -> PagingAsync.Fail(isFullPage, async.error)
            async is WorkAsync.Success && isFullPage -> PagingAsync.EmptyPage
            async is WorkAsync.Success -> PagingAsync.Success(hasMore)
            else -> PagingAsync.Uninitialized
        }
    }

    /**
     * 初始化
     */
    public fun initialize(): PagingData<T> = PagingData(pageSize = pageSize)

    public fun updateList(block: (List<T>) -> List<T>): PagingData<T> {
        val newList = block(list)

        return copy(list = newList, emptyPage = newList.isEmpty())
    }

    public fun createRefreshParam(): PagingParam<T> {
        return PagingParam(
            loadPage = PagingResponse.pageStart,
            pageSize = pageSize,
            oldList = emptyList(),
        )
    }

    public fun createNextParam(): PagingParam<T> {
        return PagingParam(
            loadPage = nextPage,
            pageSize = pageSize,
            oldList = list,
        )
    }

    internal fun updateByResponse(response: PagingResponse<T>): PagingData<T> {
        return copy(
            nextPage = response.nextPage,
            emptyPage = response.isEmptyPage,
            hasMore = response.hasMore,
            list = response.list,
            async = WorkAsync.Success,
            totalSize = response.totalSize,
            totalPage = response.totalPage,
        )
    }
}

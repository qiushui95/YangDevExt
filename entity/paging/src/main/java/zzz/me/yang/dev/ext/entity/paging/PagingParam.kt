package zzz.me.yang.dev.ext.entity.paging

public data class PagingParam<T : PagingItem>(
    val loadPage: Int,
    val pageSize: Int,
    val oldList: List<T>,
) {
    val isRefresh: Boolean
        get() = loadPage == PagingResponse.pageStart

    public fun convertResponse(
        hasMore: Boolean,
        newList: List<T>,
        totalSize: Int,
        totalPage: Int,
        curPage: Int = loadPage,
    ): PagingResponse<T> {
        val totalList = oldList + newList
        val distinctList = totalList.distinctBy { it.uniqueId }

        return PagingResponse(
            curPage = curPage,
            hasMore = hasMore,
            list = distinctList,
            totalSize = totalSize,
            totalPage = totalPage,
        )
    }

    public fun <R : PagingItem> convert(block: T.() -> R): PagingParam<R> {
        return PagingParam(
            loadPage = loadPage,
            pageSize = pageSize,
            oldList = oldList.map { it.block() },
        )
    }
}

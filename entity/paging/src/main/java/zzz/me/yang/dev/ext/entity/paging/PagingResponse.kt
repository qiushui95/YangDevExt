package zzz.me.yang.dev.ext.entity.paging

public data class PagingResponse<T>(
    val curPage: Int,
    val hasMore: Boolean,
    val list: List<T>,
    val totalSize: Int,
    val totalPage: Int,
) {
    public companion object {
        public var pageStart: Int = 1
            internal set

        public fun setStartPage(page: Int) {
            pageStart = page
        }
    }

    val nextPage: Int = curPage + 1

    val isEmptyPage: Boolean = curPage == pageStart && list.isEmpty()

    public suspend fun <R> convert(block: suspend T.(Int) -> R): PagingResponse<R> {
        return PagingResponse(
            curPage = curPage,
            hasMore = hasMore,
            list = list.mapIndexed { index, itemInfo -> itemInfo.block(index) },
            totalSize = totalSize,
            totalPage = totalPage,
        )
    }
}

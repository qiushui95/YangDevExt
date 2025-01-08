package zzz.me.yang.dev.ext.entity.paging

public data class PagingResponse<T>(
    val curPage: Int,
    val hasMore: Boolean,
    val list: List<T>,
    val totalSize: Int,
    val totalPage: Int,
) {
    public fun <R> convert(block: T.(Int) -> R?): PagingResponse<R> {
        return PagingResponse(
            curPage = curPage,
            hasMore = hasMore,
            list = list.mapIndexedNotNull { index, itemInfo -> itemInfo.block(index) },
            totalSize = totalSize,
            totalPage = totalPage,
        )
    }
}

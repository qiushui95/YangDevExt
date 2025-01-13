package zzz.me.yang.dev.ext.entity.paging

public data class SimplePagingResponse<T : PagingItem>(
    override val curPage: Int,
    override val hasMore: Boolean,
    override val list: List<T>,
    override val totalSize: Int,
    override val totalPage: Int,
) : PagingResponse<T> {
    public override fun <R : PagingItem> convert(block: T.() -> R?): SimplePagingResponse<R> {
        return SimplePagingResponse(
            curPage = curPage,
            hasMore = hasMore,
            list = list.mapNotNull { it.block() },
            totalSize = totalSize,
            totalPage = totalPage,
        )
    }

    override fun update(
        curPage: Int,
        hasMore: Boolean,
        list: List<T>,
        totalSize: Int,
        totalPage: Int,
    ): PagingResponse<T> {
        return SimplePagingResponse(
            curPage = curPage,
            hasMore = hasMore,
            list = list,
            totalSize = totalSize,
            totalPage = totalPage,
        )
    }
}

package zzz.me.yang.dev.ext.entity.paging

public interface PagingResponse<T : PagingItem> {
    public val curPage: Int
    public val hasMore: Boolean
    public val list: List<T>
    public val totalSize: Int
    public val totalPage: Int

    public fun <R : PagingItem> convert(block: T.() -> R?): PagingResponse<R>

    public fun update(
        curPage: Int = this.curPage,
        hasMore: Boolean = this.hasMore,
        list: List<T> = this.list,
        totalSize: Int = this.totalSize,
        totalPage: Int = this.totalPage,
    ): PagingResponse<T>
}

package zzz.me.yang.dev.ext.entity.paging

public data class RefreshPagingResponse<Info, Item : PagingItem>(
    val info: Info,
    val pagingResponse: PagingResponse<Item>,
) : PagingResponse<Item> by pagingResponse {
    override fun <R : PagingItem> convert(block: Item.() -> R?): PagingResponse<R> {
        return RefreshPagingResponse(info, pagingResponse.convert(block))
    }
}

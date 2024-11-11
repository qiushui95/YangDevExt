package zzz.me.yang.dev.ext.vm.core.paging

import zzz.me.yang.dev.ext.entity.paging.PagingItem

public interface PagingItemConverter<T : PagingItem> : PagingItem {
    public fun convert(): T
}

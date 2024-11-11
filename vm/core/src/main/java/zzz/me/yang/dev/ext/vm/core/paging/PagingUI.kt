package zzz.me.yang.dev.ext.vm.core.paging

import zzz.me.yang.dev.ext.entity.paging.PagingItem
import zzz.me.yang.dev.ext.vm.core.ui.VMUI

public interface PagingUI<UI : VMUI, Item : PagingItem> : VMUI {
    public fun getPagingData(): PagingData<Item>

    public fun updatePagingData(pagingData: PagingData<Item>): UI
}

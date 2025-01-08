package zzz.me.yang.dev.ext.vm.core.paging

import zzz.me.yang.dev.ext.entity.paging.PagingItem
import zzz.me.yang.dev.ext.vm.core.ui.VMUI

public interface PagingUI<Item : PagingItem> : VMUI {
    public val pagingData: PagingData<Item>
}

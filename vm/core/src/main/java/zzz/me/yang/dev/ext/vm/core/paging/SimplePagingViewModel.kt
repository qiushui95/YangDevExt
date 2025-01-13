package zzz.me.yang.dev.ext.vm.core.paging

import org.koin.core.Koin
import zzz.me.yang.dev.ext.entity.paging.PagingItem
import zzz.me.yang.dev.ext.vm.core.action.VMAction
import zzz.me.yang.dev.ext.vm.core.args.BasePageArgs
import zzz.me.yang.dev.ext.vm.core.intent.VMIntent

public abstract class SimplePagingViewModel<U, ITEM : PagingItem, I, A, Args>(
    koin: Koin,
    args: Args,
    initialUI: U,
) : BasePagingViewModel<U, ITEM, ITEM, I, A, Args>(koin, args, initialUI)
    where U : PagingUI<ITEM>, I : VMIntent<U, I, A, Args>, A : VMAction, Args : BasePageArgs {
    override fun convert(uiInfo: U, itemInfo: ITEM, oldItem: ITEM?): ITEM? {
        return itemInfo
    }

    override fun convert2(uiInfo: U, itemInfo: ITEM): ITEM? {
        return itemInfo
    }
}

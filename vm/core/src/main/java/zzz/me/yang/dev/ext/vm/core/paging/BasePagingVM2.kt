package zzz.me.yang.dev.ext.vm.core.paging

import org.koin.core.Koin
import zzz.me.yang.dev.ext.entity.paging.PagingItem
import zzz.me.yang.dev.ext.entity.paging.PagingParam
import zzz.me.yang.dev.ext.entity.paging.PagingResponse
import zzz.me.yang.dev.ext.vm.core.action.VMAction
import zzz.me.yang.dev.ext.vm.core.args.BasePageArgs
import zzz.me.yang.dev.ext.vm.core.intent.VMIntent
import zzz.me.yang.dev.ext.vm.core.ui.VMUI

private typealias VMMapper<T> = PagingItemConverter<T>

public abstract class BasePagingVM2<U, ITEM : VMMapper<ITEM2>, ITEM2 : PagingItem, I, A, Args>(
    koin: Koin,
    args: Args,
    initialState: U,
) : BasePagingVM<U, ITEM, I, A, Args>(koin, args, initialState)
    where U : VMUI, I : VMIntent<U, I, A, Args>, A : VMAction, Args : BasePageArgs {
    final override suspend fun getPagingResponseBlock(
        uiInfo: U,
        pagingParam: PagingParam<ITEM>,
    ): (suspend () -> PagingResponse<ITEM>)? {
        val param2 = pagingParam.convert { convert() }

        val responseBlock = getPagingResponseBlock2(
            uiInfo = uiInfo,
            pagingParam = param2,
        ) ?: return null

        return { responseBlock().convert { mapper() } }
    }

    protected abstract suspend fun ITEM2.mapper(): ITEM

    protected abstract suspend fun getPagingResponseBlock2(
        uiInfo: U,
        pagingParam: PagingParam<ITEM2>,
    ): (suspend () -> PagingResponse<ITEM2>)?
}

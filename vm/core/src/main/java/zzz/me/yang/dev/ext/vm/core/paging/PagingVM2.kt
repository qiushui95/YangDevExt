package zzz.me.yang.dev.ext.vm.core.paging

import zzz.me.yang.dev.ext.entity.paging.PagingItem
import zzz.me.yang.dev.ext.entity.paging.PagingParam
import zzz.me.yang.dev.ext.vm.core.action.VMAction
import zzz.me.yang.dev.ext.vm.core.args.BasePageArgs
import zzz.me.yang.dev.ext.vm.core.intent.VMIntent
import zzz.me.yang.dev.ext.vm.core.ui.VMUI

private typealias VMMapper<T> = PagingItemConverter<T>

public interface PagingVM2<U : VMUI, M1, M2, I, A : VMAction, Args> : PagingVM<U, M1, I, A, Args>
    where M1 : VMMapper<M2>, M2 : PagingItem, I : VMIntent<U, I, A, Args>, Args : BasePageArgs {
    override suspend fun getPagingResponseBlock(
        uiInfo: U,
        pagingParam: PagingParam<M1>,
    ): PagingResponseBlock<M1>? {
        val param2 = pagingParam.convert { convert() }

        val responseBlock = getPagingResponseBlock2(
            uiInfo = uiInfo,
            pagingParam = param2,
        ) ?: return null

        return { responseBlock().convert { mapper(uiInfo) } }
    }

    public suspend fun M2.mapper(uiInfo: U): M1

    public suspend fun getPagingResponseBlock2(
        uiInfo: U,
        pagingParam: PagingParam<M2>,
    ): PagingResponseBlock<M2>?
}

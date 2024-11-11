package zzz.me.yang.dev.ext.vm.core.paging

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.Koin
import pro.respawn.flowmvi.dsl.StoreBuilder
import zzz.me.yang.dev.ext.entity.paging.PagingItem
import zzz.me.yang.dev.ext.entity.paging.PagingParam
import zzz.me.yang.dev.ext.entity.paging.PagingResponse
import zzz.me.yang.dev.ext.vm.core.BaseVM
import zzz.me.yang.dev.ext.vm.core.Pipeline
import zzz.me.yang.dev.ext.vm.core.action.VMAction
import zzz.me.yang.dev.ext.vm.core.args.BasePageArgs
import zzz.me.yang.dev.ext.vm.core.async.Async
import zzz.me.yang.dev.ext.vm.core.intent.PagingIntent
import zzz.me.yang.dev.ext.vm.core.intent.VMIntent
import zzz.me.yang.dev.ext.vm.core.plugin.whileCanInit
import zzz.me.yang.dev.ext.vm.core.ui.VMUI
import zzz.me.yang.dev.ext.vm.core.work.WorkStrategy

public abstract class BasePagingVM<U, ITEM : PagingItem, I, A, Args>(
    koin: Koin,
    args: Args,
    initialUI: U,
) : BaseVM<U, I, A, Args>(koin, args, initialUI)
    where U : VMUI, I : VMIntent<U, I, A, Args>, A : VMAction, Args : BasePageArgs {
    private companion object {
        const val KEY_LOAD_DATA_PAGING = "loadPaging"
    }

    override fun StoreBuilder<U, I, A>.configLast() {
        whileCanInit {
            if (needLoadPagingInit) {
                intent(providePagingIntent(PagingIntent.LoadInit()))
            }
        }
    }

    protected open val needLoadPagingInit: Boolean = true

    public abstract fun U.mapper(): PagingData<ITEM>

    protected abstract suspend fun U.mapper(pagingData: PagingData<ITEM>): U

    protected abstract suspend fun getPagingResponseBlock(
        uiInfo: U,
        pagingParam: PagingParam<ITEM>,
    ): (suspend () -> PagingResponse<ITEM>)?

    private fun U.canLoadPaging(isRefresh: Boolean): Boolean {
        return if (isRefresh) {
            true
        } else {
            mapper().hasMore
        }
    }

    protected open suspend fun loadPagingData(
        workStrategy: WorkStrategy,
        isRefresh: Boolean,
        pagingParamBlock: U.() -> PagingParam<ITEM>,
    ) {
        beforeSuspendWork(
            workStrategy = workStrategy,
            key = KEY_LOAD_DATA_PAGING,
            interval = 0,
        ) {
            viewModelScope.launch(Dispatchers.IO + it) {
                val uiInfo = withStateResult { this }

                if (uiInfo.canLoadPaging(isRefresh)) {
                    try {
                        loadPagingData(
                            isRefresh = isRefresh,
                            pagingParamBlock = pagingParamBlock,
                        )
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        updateState { mapper(mapper().copy(async = Async.Fail(ex))) }
                    }
                }
            }
        }
    }

    private suspend fun loadPagingData(
        isRefresh: Boolean,
        pagingParamBlock: U.() -> PagingParam<ITEM>,
    ) {
        withState {
            val pagingParam = pagingParamBlock()

            val responseBlock = getPagingResponseBlock(this, pagingParam) ?: return@withState

            updateState { mapper(mapper().copy(async = Async.Loading, isRefresh = isRefresh)) }

            updateState { mapper(mapper().updateByResponse(responseBlock.invoke())) }
        }
    }

    private fun U.createPagingParam(isRefresh: Boolean): PagingParam<ITEM> {
        val pagingData = mapper()

        val loadPage = if (isRefresh) {
            PagingResponse.pageStart
        } else {
            pagingData.nextPage
        }

        val oldList = if (isRefresh) {
            emptyList()
        } else {
            pagingData.list
        }

        return PagingParam(
            loadPage = loadPage,
            pageSize = pagingData.pageSize,
            oldList = oldList,
        )
    }

    protected suspend fun refresh(initPagingData: Boolean) {
        if (initPagingData) {
            updateState { mapper(mapper().initialize()) }
        }

        loadPagingData(
            workStrategy = WorkStrategy.CancelBefore,
            isRefresh = true,
            pagingParamBlock = { createPagingParam(true) },
        )
    }

    private suspend fun loadNextPage() {
        loadPagingData(
            workStrategy = WorkStrategy.CancelBefore,
            isRefresh = false,
            pagingParamBlock = { createPagingParam(false) },
        )
    }

    public final override suspend fun handlePagingIntent(
        pipeline: Pipeline<U, I, A>,
        intent: PagingIntent,
    ) {
        when (intent) {
            is PagingIntent.LoadNextPage -> loadNextPage()
            is PagingIntent.Refresh -> refresh(false)
            is PagingIntent.LoadInit -> refresh(true)
        }
    }
}

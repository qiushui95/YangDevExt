package zzz.me.yang.dev.ext.vm.core.paging

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.dsl.StoreBuilder
import zzz.me.yang.dev.ext.entity.paging.PagingItem
import zzz.me.yang.dev.ext.entity.paging.PagingParam
import zzz.me.yang.dev.ext.entity.paging.PagingResponse
import zzz.me.yang.dev.ext.vm.core.Pipeline
import zzz.me.yang.dev.ext.vm.core.action.VMAction
import zzz.me.yang.dev.ext.vm.core.args.BasePageArgs
import zzz.me.yang.dev.ext.vm.core.async.Async
import zzz.me.yang.dev.ext.vm.core.handler.VMHandler
import zzz.me.yang.dev.ext.vm.core.intent.CommonIntent
import zzz.me.yang.dev.ext.vm.core.intent.PagingIntent
import zzz.me.yang.dev.ext.vm.core.intent.VMIntent
import zzz.me.yang.dev.ext.vm.core.plugin.whileCanInit
import zzz.me.yang.dev.ext.vm.core.ui.VMUI
import zzz.me.yang.dev.ext.vm.core.work.WorkStrategy
import zzz.me.yang.dev.ext.vm.core.work.WorkStrategyChecker

public typealias PagingResponseBlock<ITEM> = suspend () -> PagingResponse<ITEM>

public interface PagingVM<U : VMUI, ITEM, I, A : VMAction, Args : BasePageArgs>
    where  ITEM : PagingItem, I : VMIntent<U, I, A, Args> {

    public fun getVMHandler(): VMHandler<U, I, A, Args>

    public fun getStrategyChecker(): WorkStrategyChecker

    public val needLoadPagingInit: Boolean

    public fun configLastStore(storeBuilder: StoreBuilder<U, I, A>) {
        storeBuilder.whileCanInit {
            if (needLoadPagingInit) {
                val handler = getVMHandler()

                val commonIntent = CommonIntent.OnPaging(PagingIntent.LoadInit())

                handler.intent(this, handler.provideCommonIntent(commonIntent))
            }
        }
    }

    public fun U.mapper(): PagingData<ITEM>

    public suspend fun U.mapper(pagingData: PagingData<ITEM>): U

    public suspend fun getPagingResponseBlock(
        uiInfo: U,
        pagingParam: PagingParam<ITEM>
    ): PagingResponseBlock<ITEM>?


    private fun U.canLoadPaging(isRefresh: Boolean): Boolean {
        return if (isRefresh) true else mapper().hasMore
    }


    public suspend fun loadPagingData(
        pipeline: Pipeline<U, I, A>,
        workStrategy: WorkStrategy,
        isRefresh: Boolean,
        pagingParamBlock: U.() -> PagingParam<ITEM>,
    ) {
        getStrategyChecker().startCheck(workStrategy, "loadPaging") { job ->
            getVMHandler().getIoScope().launch(Dispatchers.IO + job) {
                val uiInfo = getVMHandler().withStateResult { this }

                if (uiInfo.canLoadPaging(isRefresh)) {
                    try {
                        loadPagingData(
                            pipeline = pipeline,
                            isRefresh = isRefresh,
                            pagingParamBlock = pagingParamBlock,
                        )
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        pipeline.updateState { mapper(mapper().copy(async = Async.Fail(ex))) }
                    }
                }
            }
        }
    }

    private suspend fun loadPagingData(
        pipeline: Pipeline<U, I, A>,
        isRefresh: Boolean,
        pagingParamBlock: U.() -> PagingParam<ITEM>,
    ) {
        pipeline.withState {
            val pagingParam = pagingParamBlock()

            val responseBlock = getPagingResponseBlock(this, pagingParam) ?: return@withState

            pipeline.updateState {
                mapper(mapper().copy(async = Async.Loading, isRefresh = isRefresh))
            }

            pipeline.updateState { mapper(mapper().updateByResponse(responseBlock.invoke())) }
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


    public suspend fun refresh(pipeline: Pipeline<U, I, A>, initPagingData: Boolean) {
        if (initPagingData) {
            pipeline.updateState { mapper(mapper().initialize()) }
        }

        loadPagingData(
            pipeline = pipeline,
            workStrategy = WorkStrategy.CancelBefore,
            isRefresh = true,
            pagingParamBlock = { createPagingParam(true) },
        )
    }

    private suspend fun loadNextPage(pipeline: Pipeline<U, I, A>) {
        loadPagingData(
            pipeline = pipeline,
            workStrategy = WorkStrategy.CancelBefore,
            isRefresh = false,
            pagingParamBlock = { createPagingParam(false) },
        )
    }


    public suspend fun handlePagingIntent(pipeline: Pipeline<U, I, A>, intent: PagingIntent) {
        when (intent) {
            is PagingIntent.LoadNextPage -> loadNextPage(pipeline)
            is PagingIntent.Refresh -> refresh(pipeline, false)
            is PagingIntent.LoadInit -> refresh(pipeline, true)
        }
    }
}
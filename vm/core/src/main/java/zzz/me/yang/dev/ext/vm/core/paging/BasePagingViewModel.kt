package zzz.me.yang.dev.ext.vm.core.paging

import org.koin.core.Koin
import pro.respawn.flowmvi.dsl.StoreBuilder
import zzz.me.yang.dev.ext.entity.paging.PagingItem
import zzz.me.yang.dev.ext.entity.paging.PagingParam
import zzz.me.yang.dev.ext.entity.paging.PagingResponse
import zzz.me.yang.dev.ext.vm.core.BaseViewModel
import zzz.me.yang.dev.ext.vm.core.Pipeline
import zzz.me.yang.dev.ext.vm.core.action.VMAction
import zzz.me.yang.dev.ext.vm.core.args.BasePageArgs
import zzz.me.yang.dev.ext.vm.core.intent.CommonIntent
import zzz.me.yang.dev.ext.vm.core.intent.PagingIntent
import zzz.me.yang.dev.ext.vm.core.intent.VMIntent
import zzz.me.yang.dev.ext.vm.core.plugin.whileCanInit
import zzz.me.yang.dev.ext.vm.core.work.WorkAsync
import zzz.me.yang.dev.ext.vm.core.work.WorkStrategy

public abstract class BasePagingViewModel<U, IT : PagingItem, IT2 : PagingItem, I, A, Args>(
    koin: Koin,
    args: Args,
    initialUI: U,
) : BaseViewModel<U, I, A, Args>(koin, args, initialUI)
    where U : PagingUI<IT>, I : VMIntent<U, I, A, Args>, A : VMAction, Args : BasePageArgs {
    private companion object {
        const val KEY_PAGING = "loadPaging"
    }

    protected open val needLoadPagingInit: Boolean = true

    override fun StoreBuilder<U, I, A>.configLast() {
        whileCanInit { onInit() }
    }

    private fun onInit() {
        if (!needLoadPagingInit) return

        val commonIntent = CommonIntent.OnPaging(PagingIntent.LoadInit())

        intent(provideCommonIntent(commonIntent))
    }

    override suspend fun handleCommonIntent(pipeline: Pipeline<U, I, A>, intent: CommonIntent) {
        super.handleCommonIntent(pipeline, intent)

        if (intent is CommonIntent.OnPaging) handlePagingIntent(pipeline, intent.pagingIntent)
    }

    protected open fun canLoadPaging(uiInfo: U): Boolean {
        return true
    }

    public abstract suspend fun loadPagingResponse(
        uiInfo: U,
        pagingParam: PagingParam<IT2>,
    ): PagingResponse<IT2>

    private suspend fun handlePagingIntent(pipeline: Pipeline<U, I, A>, intent: PagingIntent) {
        when (intent) {
            is PagingIntent.LoadNextPage -> loadNextPage(pipeline)
            is PagingIntent.Refresh -> refresh(pipeline, intent)
            is PagingIntent.LoadInit -> refresh(pipeline, intent)
        }
    }

    public abstract fun convert(uiInfo: U, itemInfo: IT2, oldItem: IT?): IT?

    public abstract fun convert2(uiInfo: U, itemInfo: IT): IT2?

    public open fun U.pagingSuccessMapper(response: PagingResponse<IT>): U = this

    public open suspend fun onPagingStart(pipeline: Pipeline<U, I, A>, intentFromUI: Boolean) {}

    public open suspend fun onPagingSuccess(
        pipeline: Pipeline<U, I, A>,
        intentFromUI: Boolean,
        response: PagingResponse<IT>,
    ) {
    }

    public open suspend fun onPagingFail(
        pipeline: Pipeline<U, I, A>,
        intentFromUI: Boolean,
        error: Throwable,
    ) {
    }

    public open suspend fun onPagingEnd(pipeline: Pipeline<U, I, A>, intentFromUI: Boolean) {}

    private suspend fun startLoadPagingResponse(
        pipeline: Pipeline<U, I, A>,
        intentFromUI: Boolean,
        workStrategy: WorkStrategy,
        isRefresh: Boolean,
    ) {
        startSuspendWork(
            key = KEY_PAGING,
            workStrategy = workStrategy,
            canContinue = { canLoadPaging(this) && (isRefresh || pagingData.hasMore) },
            successMapper = { pagingSuccessMapper(it) },
            onStart = { pagingData.updateWorkAsync(WorkAsync.Loading) },
            onStart2 = { onPagingStart(pipeline, intentFromUI) },
            onSuccess = { pagingData.updateByResponse(it) },
            onSuccess2 = { onPagingSuccess(pipeline, intentFromUI, it) },
            onFail = { pagingData.updateWorkAsync(WorkAsync.Fail(it)) },
            onFail2 = { onPagingFail(pipeline, intentFromUI, it) },
            onEnd2 = { onPagingEnd(pipeline, intentFromUI) },
        ) { uiInfo ->

            val param = uiInfo.pagingData.createPagingParam(isRefresh)
                .convert { convert2(uiInfo, this) }

            loadPagingResponse(uiInfo, param).convert {
                val oldItem = uiInfo.pagingData.itemList.firstOrNull { it.uniqueId == uniqueId }

                convert(uiInfo, this, oldItem)
            }
        }
    }

    private suspend fun refresh(pipeline: Pipeline<U, I, A>, intent: PagingIntent) {
        val intentFromUI: Boolean
        val initPagingData: Boolean

        when (intent) {
            is PagingIntent.LoadInit -> {
                intentFromUI = false
                initPagingData = true
            }

            is PagingIntent.LoadNextPage -> return

            is PagingIntent.Refresh -> {
                intentFromUI = intent.fromUI
                initPagingData = intent.initPagingData
            }
        }

        if (initPagingData) pipeline.withState { pagingData.initialize() }

        startLoadPagingResponse(
            pipeline = pipeline,
            intentFromUI = intentFromUI,
            workStrategy = WorkStrategy.CancelBefore,
            isRefresh = true,
        )
    }

    private suspend fun loadNextPage(pipeline: Pipeline<U, I, A>) {
        startLoadPagingResponse(
            pipeline = pipeline,
            intentFromUI = true,
            workStrategy = WorkStrategy.CancelCurrent,
            isRefresh = false,
        )
    }
}

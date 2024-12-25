package zzz.me.yang.dev.ext.vm.core

import androidx.annotation.CallSuper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.Koin
import pro.respawn.flowmvi.api.ActionShareBehavior
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.StateProvider
import pro.respawn.flowmvi.api.StateReceiver
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.dsl.StoreConfigurationBuilder
import pro.respawn.flowmvi.dsl.lazyStore
import pro.respawn.flowmvi.plugins.recover
import pro.respawn.flowmvi.plugins.reduce
import zzz.me.yang.dev.ext.vm.core.action.CommonAction
import zzz.me.yang.dev.ext.vm.core.action.VMAction
import zzz.me.yang.dev.ext.vm.core.args.BasePageArgs
import zzz.me.yang.dev.ext.vm.core.handler.VMHandler
import zzz.me.yang.dev.ext.vm.core.intent.CommonIntent
import zzz.me.yang.dev.ext.vm.core.intent.PagingIntent
import zzz.me.yang.dev.ext.vm.core.intent.VMIntent
import zzz.me.yang.dev.ext.vm.core.interval.IntervalChecker
import zzz.me.yang.dev.ext.vm.core.interval.IntervalCheckerImpl
import zzz.me.yang.dev.ext.vm.core.plugin.whileCanInit
import zzz.me.yang.dev.ext.vm.core.ui.VMUI
import zzz.me.yang.dev.ext.vm.core.work.WorkAsync
import zzz.me.yang.dev.ext.vm.core.work.WorkStrategy
import zzz.me.yang.dev.ext.vm.core.work.WorkStrategyChecker
import zzz.me.yang.dev.ext.vm.core.work.WorkStrategyCheckerImpl

public abstract class BaseViewModel<U : VMUI, I, A : VMAction, Args : BasePageArgs>(
    private val koin: Koin,
    private val args: Args,
    initialUI: U,

) : ViewModel(), VMHandler<U, I, A, Args>, IntervalChecker by IntervalCheckerImpl()
    where I : VMIntent<U, I, A, Args> {
    private val _ioScope: CoroutineScope by lazy {
        object : CoroutineScope {
            override val coroutineContext = viewModelScope.coroutineContext + Dispatchers.IO
        }
    }

    protected val workStrategyChecker: WorkStrategyChecker by lazy { WorkStrategyCheckerImpl() }

    private val intervalJob = SupervisorJob()

    private val iaDispatcher = Dispatchers.IO.limitedParallelism(1)

    public val store: Store<U, I, A> by lazyStore(initialUI, viewModelScope) {
        configFirst()

        configure { configure() }

        reduce { intent ->
            reduceByCatch(intent, this)
        }

        recover { ex ->
            intentError(ex)
            null
        }

        whileCanInit {
            onInit()
        }

        configLast()
    }

    protected open fun StoreConfigurationBuilder.configure() {
        parallelIntents = false
        coroutineContext = Dispatchers.Default
        actionShareBehavior = ActionShareBehavior.Share()
        intentCapacity = ActionShareBehavior.DefaultBufferSize
    }

    @Suppress("UNCHECKED_CAST")
    protected suspend fun updateState(transform: suspend U.() -> U) {
        (store as StateReceiver<U>).updateState(transform)
    }

    @Suppress("UNCHECKED_CAST")
    protected suspend fun withState(block: suspend U.() -> Unit) {
        (store as StateReceiver<U>).withState(block)
    }

    @Suppress("UNCHECKED_CAST")
    public val states: StateFlow<U> = (store as StateProvider<U>).states

    protected open fun StoreBuilder<U, I, A>.configFirst() {
    }

    protected open fun StoreBuilder<U, I, A>.configLast() {
    }

    private fun onInit() {
        intentCommon(CommonIntent.OnInit())
    }

    private suspend fun reduceByCatch(intent: I, pipeline: PipelineContext<U, I, A>) {
        try {
            reduceIntent(intent, pipeline)
        } catch (ex: Throwable) {
            intentError(ex)
        }
    }

    @CallSuper
    protected open suspend fun reduceIntent(intent: I, pipeline: PipelineContext<U, I, A>) {
        intent.apply { reduce(pipeline) }
    }

    public override suspend fun <R> withStateResult(block: suspend U.() -> R): R {
        var result: R? = null
        withState {
            result = block()
        }

        return result!!
    }

    private fun getFailBlock(
        onFail: (suspend (Throwable) -> Unit)?,
    ): (suspend (Throwable) -> Unit) {
        return onFail ?: { this.intentError(it) }
    }

    public override suspend fun <T> startSuspendWork(
        key: String,
        workStrategy: WorkStrategy,
        startMapper: (U.() -> U)?,
        dataMapper: (U.(T) -> U)?,
        failMapper: (U.(Throwable) -> U)?,
        endMapper: (U.() -> U)?,
        onStart: (suspend () -> Unit)?,
        onStart2: (suspend () -> Unit)?,
        onFail: (suspend (Throwable) -> Unit)?,
        onFail2: (suspend (Throwable) -> Unit)?,
        onSuccess: (suspend (T) -> Unit)?,
        onSuccess2: (suspend (T) -> Unit)?,
        onEnd: (suspend () -> Unit)?,
        block: suspend (U) -> T,
    ): Job? {
        return workStrategyChecker.startCheck(workStrategy, key) { job ->
            doSuspendWork(
                job = job,
                asyncMapper = null,
                startMapper = startMapper,
                dataMapper = dataMapper,
                failMapper = failMapper,
                endMapper = endMapper,
                onStartList = listOfNotNull(onStart, onStart2),
                onFailList = listOfNotNull(getFailBlock(onFail), onFail2),
                onSuccessList = listOfNotNull(onSuccess, onSuccess2),
                onEnd = onEnd,
                block = block,
            )
        }
    }

    public override suspend fun <T> startSuspendWork(
        canContinue: U.() -> Boolean,
        asyncMapper: U.(WorkAsync) -> U,
        startMapper: (U.() -> U)?,
        dataMapper: (U.(T) -> U)?,
        failMapper: (U.(Throwable) -> U)?,
        endMapper: (U.() -> U)?,
        onStart: (suspend () -> Unit)?,
        onStart2: (suspend () -> Unit)?,
        onFail: (suspend (Throwable) -> Unit)?,
        onFail2: (suspend (Throwable) -> Unit)?,
        onSuccess: (suspend (T) -> Unit)?,
        onSuccess2: (suspend (T) -> Unit)?,
        onEnd: (suspend () -> Unit)?,
        block: suspend (U) -> T,
    ): Job? {
        val uiInfo = withStateResult { this }

        if (uiInfo.canContinue().not()) return null

        return doSuspendWork(
            job = null,
            asyncMapper = asyncMapper,
            startMapper = startMapper,
            dataMapper = dataMapper,
            failMapper = failMapper,
            endMapper = endMapper,
            onStartList = listOfNotNull(onStart, onStart2),
            onFailList = listOfNotNull(getFailBlock(onFail), onFail2),
            onSuccessList = listOfNotNull(onSuccess, onSuccess2),
            onEnd = onEnd,
            block = block,
        )
    }

    private suspend fun updateAsync(
        asyncMapper: (U.(WorkAsync) -> U)?,
        asyncBlock: () -> WorkAsync,
    ) {
        asyncMapper ?: return

        updateState { asyncMapper(asyncBlock()) }
    }

    private fun <T> doSuspendWork(
        job: Job?,
        asyncMapper: (U.(WorkAsync) -> U)?,
        startMapper: (U.() -> U)?,
        dataMapper: (U.(T) -> U)?,
        failMapper: (U.(Throwable) -> U)?,
        endMapper: (U.() -> U)?,
        onStartList: List<suspend () -> Unit>,
        onFailList: List<suspend (Throwable) -> Unit>,
        onSuccessList: List<(suspend (T) -> Unit)>,
        onEnd: (suspend () -> Unit)?,
        block: suspend (U) -> T,
    ) = viewModelScope.launch(Dispatchers.IO + (job ?: SupervisorJob())) {
        try {
            onStartList.forEach { it.invoke() }

            updateAsync(asyncMapper) { WorkAsync.Loading }

            if (startMapper != null) {
                updateState { startMapper() }
            }

            val uiInfo = withStateResult { this }

            val result = block(uiInfo)

            if (dataMapper != null) {
                updateState { dataMapper(result) }
            }

            onSuccessList.forEach { it.invoke(result) }

            updateAsync(asyncMapper) { WorkAsync.Success }
        } catch (ex: Throwable) {
            updateAsync(asyncMapper) { WorkAsync.Fail(ex) }

            if (onFailList.isEmpty()) {
                intentError(ex)
            } else {
                onFailList.forEach { it(ex) }
            }

            if (failMapper != null) {
                updateState { failMapper(ex) }
            }
        } finally {
            if (endMapper != null) {
                updateState { endMapper() }
            }

            onEnd?.invoke()
        }
    }

    override fun getKoin(): Koin {
        return koin
    }

    override fun getIoScope(): CoroutineScope {
        return _ioScope
    }

    override fun getArgs(): Args {
        return args
    }

    @CallSuper
    public override suspend fun handleCommonIntent(
        pipeline: Pipeline<U, I, A>,
        intent: CommonIntent,
    ) {
        when (intent) {
            is CommonIntent.OnError -> reduceError(pipeline, intent.error)
            is CommonIntent.OnInit -> reduceInit(pipeline)
            is CommonIntent.OnLifecycle -> reduceLifecycle(pipeline, intent.event)
            is CommonIntent.OnBackPressed -> reduceBack(pipeline)
            is CommonIntent.OnPaging -> {}
        }

        if (intent is CommonIntent.OnLifecycle) {
            val event = intent.event

            if (event == Lifecycle.Event.ON_RESUME) {
                for (childJob in intervalJob.children) {
                    if (childJob.isActive) return
                }

                getIoScope().launch(intervalJob) {
                    startIntervalJob(pipeline, this)
                }
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                cancelIntervalJob(pipeline)
            }
        }
    }

    protected open suspend fun startIntervalJob(
        pipeline: Pipeline<U, I, A>,
        scope: CoroutineScope,
    ) {
    }

    @CallSuper
    protected open suspend fun cancelIntervalJob(pipeline: Pipeline<U, I, A>) {
        intervalJob.cancelChildren()
    }

    override fun intent(vararg intent: I?) {
        viewModelScope.launch(iaDispatcher) {
            for (i in intent) {
                i ?: continue
                checkInterval(i) { store.intent(i) }
            }
        }
    }

    override fun intentCommon(info: CommonIntent?) {
        intent(provideCommonIntent(info ?: return))
    }

    override fun intentInit() {
        intentCommon(CommonIntent.OnInit())
    }

    override fun intentError(error: Throwable?) {
        error ?: return

        if (error.javaClass.name == "kotlinx.coroutines.JobCancellationException") return

        intentCommon(CommonIntent.OnError(error))
    }

    override fun intentPaging(info: PagingIntent?) {
        intent(provideCommonIntent(CommonIntent.OnPaging(info ?: return)))
    }

    override fun intentPagingRefresh() {
        intentPaging(PagingIntent.Refresh())
    }

    override fun action(pipeline: Pipeline<U, I, A>, vararg action: A?) {
        viewModelScope.launch(iaDispatcher) {
            for (a in action) {
                a ?: continue
                checkInterval(a) { pipeline.action(a) }
            }
        }
    }

    override fun actionCommon(pipeline: Pipeline<U, I, A>, info: CommonAction?) {
        action(pipeline, provideCommonAction(info ?: return))
    }

    override fun getToastAction(res: Int?, isShort: Boolean): A? {
        res ?: return null

        val toastInfo = ToastInfo.Res(res, isShort)

        return provideCommonAction(CommonAction.Toast(toastInfo))
    }

    override fun actionToast(pipeline: Pipeline<U, I, A>, res: Int?, isShort: Boolean) {
        action(pipeline, getToastAction(res, isShort))
    }

    override fun getToastAction(text: String?, isShort: Boolean): A? {
        text ?: return null

        val toastInfo = ToastInfo.Text(text, isShort)

        return provideCommonAction(CommonAction.Toast(toastInfo))
    }

    override fun actionToast(pipeline: Pipeline<U, I, A>, text: String?, isShort: Boolean) {
        action(pipeline, getToastAction(text, isShort))
    }

    override fun getBackAction(res: Int?): A? {
        val toastInfo = res?.run { ToastInfo.Res(this) }

        return provideCommonAction(CommonAction.Back(toastInfo = toastInfo))
    }

    override fun actionBack(pipeline: Pipeline<U, I, A>, res: Int?) {
        action(pipeline, getBackAction(res))
    }

    override fun getPageAction(args: BasePageArgs?): A? {
        args ?: return null

        return provideCommonAction(CommonAction.LaunchPage(args))
    }

    override fun actionPage(pipeline: Pipeline<U, I, A>, args: BasePageArgs?) {
        action(pipeline, getPageAction(args))
    }

    public fun onLifecycleEvent(event: Lifecycle.Event) {
        intentCommon(CommonIntent.OnLifecycle(event))
    }

    protected open suspend fun reduceError(pipeline: Pipeline<U, I, A>, error: Throwable) {
        error.printStackTrace()

        actionCommon(pipeline, CommonAction.Error(error))
    }

    protected open suspend fun reduceInit(pipeline: Pipeline<U, I, A>) {}

    protected open suspend fun reduceLifecycle(
        pipeline: Pipeline<U, I, A>,
        event: Lifecycle.Event,
    ) {
    }

    protected open suspend fun reduceBack(pipeline: Pipeline<U, I, A>) {
        actionBack(pipeline)
    }
}

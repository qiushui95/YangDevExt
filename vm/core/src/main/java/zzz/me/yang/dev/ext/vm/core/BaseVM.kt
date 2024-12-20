package zzz.me.yang.dev.ext.vm.core

import androidx.annotation.CallSuper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.CacheMemoryStaticUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.Koin
import pro.respawn.flowmvi.api.ActionReceiver
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
import zzz.me.yang.dev.ext.vm.core.async.Async
import zzz.me.yang.dev.ext.vm.core.handler.VMHandler
import zzz.me.yang.dev.ext.vm.core.intent.CommonIntent
import zzz.me.yang.dev.ext.vm.core.intent.PagingIntent
import zzz.me.yang.dev.ext.vm.core.intent.VMIntent
import zzz.me.yang.dev.ext.vm.core.plugin.whileCanInit
import zzz.me.yang.dev.ext.vm.core.ui.VMUI
import zzz.me.yang.dev.ext.vm.core.work.WorkStrategy

public abstract class BaseVM<U : VMUI, I, A : VMAction, Args : BasePageArgs>(
    private val koin: Koin,
    private val args: Args,
    initialUI: U,
) : ViewModel(), VMHandler<U, I, A, Args> where I : VMIntent<U, I, A, Args> {
    private val _ioScope: CoroutineScope by lazy {
        object : CoroutineScope {
            override val coroutineContext = viewModelScope.coroutineContext + Dispatchers.IO
        }
    }

    private val jobMap by lazy {
        mutableMapOf<String, Job>()
    }

    private val intervalJob = SupervisorJob()

    public val store: Store<U, I, A> by lazyStore(initialUI, viewModelScope) {
        configFirst()

        configure { configure() }

        reduce { intent ->
            reduceByCatch(intent, this)
        }

        recover { ex ->
            onErrorIntent(ex)
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

    @Suppress("UNCHECKED_CAST")
    public suspend fun action(action: A?) {
        action ?: return

        (store as ActionReceiver<A>).action(action)
    }

    public open fun intent(intent: I?) {
        intent ?: return

        store.intent(intent)
    }

    protected open fun StoreBuilder<U, I, A>.configFirst() {
    }

    protected open fun StoreBuilder<U, I, A>.configLast() {
    }

    private fun onInit() {
        val info = CommonIntent.OnInit()

        intent(provideCommonIntent(info))
    }

    private suspend fun reduceByCatch(intent: I, pipeline: PipelineContext<U, I, A>) {
        try {
            reduceIntent(intent, pipeline)
        } catch (ex: Throwable) {
            onErrorIntent(ex)
        }
    }

    @CallSuper
    protected open suspend fun reduceIntent(intent: I, pipeline: PipelineContext<U, I, A>) {
        intent.apply { reduce(pipeline) }
    }

    protected fun onErrorIntent(ex: Throwable) {
        if (ex.javaClass.name == "kotlinx.coroutines.JobCancellationException") return

        intent(provideCommonIntent(CommonIntent.OnError(ex)))
    }

    protected suspend fun <R> withStateResult(block: suspend U.() -> R): R {
        var result: R? = null
        withState {
            result = block()
        }

        return result!!
    }

    private val getWorkJobMutex = Mutex()

    private suspend fun getWorkJob(key: String): Job = getWorkJobMutex.withLock {
        jobMap.getOrPut(key) { SupervisorJob() }
    }

    protected suspend fun beforeSuspendWork(
        workStrategy: WorkStrategy?,
        key: String?,
        interval: Long = 1000L,
        workBlock: suspend (Job) -> Job,
    ): Job? {
        if (interval > 0) {
            val intervalKey = "${javaClass.name}-$key"

            val lastTime = CacheMemoryStaticUtils.get<Long>(intervalKey) ?: 0L

            val now = System.currentTimeMillis()

            if (now - lastTime < interval) return null

            CacheMemoryStaticUtils.put(intervalKey, now)
        }

        if (key == null || workStrategy == null) return workBlock(SupervisorJob())

        val workJob = getWorkJob(key)

        return checkWorkStatus(workJob = workJob, workStrategy = workStrategy) {
            workBlock(workJob)
        }
    }

    private suspend fun checkWorkStatus(
        workJob: Job,
        workStrategy: WorkStrategy,
        workBlock: suspend () -> Job,
    ): Job? {
        val jobList = workJob.children

        val hasWorkingJob = jobList.any { it.isActive }

        when {
            workStrategy is WorkStrategy.CancelCurrent && hasWorkingJob -> return null
            workStrategy is WorkStrategy.CancelBefore -> {
                jobList.forEach { it.cancelAndJoin() }
            }

            workStrategy is WorkStrategy.WaitIdle -> {
                jobList.forEach { it.join() }
            }

            else -> {}
        }

        return workBlock()
    }

    private fun getFailBlock(
        onFail: (suspend (Throwable) -> Unit)?,
    ): (suspend (Throwable) -> Unit) {
        return onFail ?: { this.onErrorIntent(it) }
    }

    public override suspend fun <T> startSuspendWork(
        key: String,
        workStrategy: WorkStrategy,
        interval: Long,
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
        return beforeSuspendWork(key = key, workStrategy = workStrategy, interval = interval) {
            doSuspendWork(
                job = it,
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
        asyncMapper: U.(Async) -> U,
        interval: Long,
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

        return beforeSuspendWork(key = null, workStrategy = null, interval = interval) {
            doSuspendWork(
                job = it,
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
    }

    private suspend fun updateAsync(asyncMapper: (U.(Async) -> U)?, asyncBlock: () -> Async) {
        asyncMapper ?: return

        updateState { asyncMapper(asyncBlock()) }
    }

    private fun <T> doSuspendWork(
        job: Job,
        asyncMapper: (U.(Async) -> U)?,
        startMapper: (U.() -> U)?,
        dataMapper: (U.(T) -> U)?,
        failMapper: (U.(Throwable) -> U)?,
        endMapper: (U.() -> U)?,
        onStartList: List<suspend () -> Unit>,
        onFailList: List<suspend (Throwable) -> Unit>,
        onSuccessList: List<(suspend (T) -> Unit)>,
        onEnd: (suspend () -> Unit)?,
        block: suspend (U) -> T,
    ) = viewModelScope.launch(Dispatchers.IO + job) {
        try {
            onStartList.forEach { it.invoke() }

            updateAsync(asyncMapper) { Async.Loading }

            if (startMapper != null) {
                updateState { startMapper() }
            }

            val uiInfo = withStateResult { this }

            val result = block(uiInfo)

            if (dataMapper != null) {
                updateState { dataMapper(result) }
            }

            onSuccessList.forEach { it.invoke(result) }

            updateAsync(asyncMapper) { Async.Success }
        } catch (ex: Throwable) {
            updateAsync(asyncMapper) { Async.Fail(ex) }

            if (onFailList.isEmpty()) {
                onErrorIntent(ex)
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

    override fun providePagingIntent(info: PagingIntent): I {
        throw NotImplementedError()
    }

    public final override suspend fun handleCommonIntent(
        pipeline: Pipeline<U, I, A>,
        intent: CommonIntent,
    ) {
        when (intent) {
            is CommonIntent.OnError -> reduceError(pipeline, intent.error)
            is CommonIntent.OnInit -> reduceInit(pipeline)
            is CommonIntent.OnLifecycle -> reduceLifecycle(pipeline, intent.event)
            is CommonIntent.OnBackPressed -> reduceBack(pipeline)
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

    override suspend fun handlePagingIntent(pipeline: Pipeline<U, I, A>, intent: PagingIntent) {
    }

    override fun intent(pipeline: Pipeline<U, I, A>, intent: I) {
        pipeline.intent(intent)
    }

    override fun intentCommon(pipeline: Pipeline<U, I, A>, info: CommonIntent) {
        intent(pipeline, provideCommonIntent(info))
    }

    override fun intentInit(pipeline: Pipeline<U, I, A>) {
        intentCommon(pipeline, CommonIntent.OnInit())
    }

    override fun intentError(pipeline: Pipeline<U, I, A>, error: Throwable) {
        intentCommon(pipeline, CommonIntent.OnError(error))
    }

    override fun intentPaging(pipeline: Pipeline<U, I, A>, info: PagingIntent) {
        pipeline.intent(providePagingIntent(info))
    }

    override fun intentPagingRefresh(pipeline: Pipeline<U, I, A>) {
        intentPaging(pipeline, PagingIntent.Refresh())
    }

    override suspend fun action(pipeline: Pipeline<U, I, A>, action: A) {
        pipeline.action(action)
    }

    override suspend fun actionCommon(pipeline: Pipeline<U, I, A>, info: CommonAction) {
        action(pipeline, provideCommonAction(info))
    }

    override suspend fun actionToast(pipeline: Pipeline<U, I, A>, res: Int?, isShort: Boolean) {
        res ?: return

        val toastInfo = ToastInfo.Res(res, isShort)

        actionCommon(pipeline, CommonAction.Toast(toastInfo))
    }

    override suspend fun actionToast(pipeline: Pipeline<U, I, A>, text: String?, isShort: Boolean) {
        text ?: return

        val toastInfo = ToastInfo.Text(text, isShort)

        actionCommon(pipeline, CommonAction.Toast(toastInfo))
    }

    override suspend fun actionBack(pipeline: Pipeline<U, I, A>, res: Int?) {
        actionToast(pipeline, res)

        actionCommon(pipeline, CommonAction.Back())
    }

    public fun onLifecycleEvent(event: Lifecycle.Event) {
        val intent = CommonIntent.OnLifecycle(event)

        intent(provideCommonIntent(intent))
    }

    protected suspend fun actionToast(pipeline: Pipeline<U, I, A>, res: Int?) {
        res ?: return

        val toastInfo = ToastInfo.Res(res)

        actionCommon(pipeline, CommonAction.Toast(toastInfo))
    }

    protected open suspend fun reduceError(pipeline: Pipeline<U, I, A>, error: Throwable) {
        actionCommon(pipeline, CommonAction.Error(error))
        error.printStackTrace()
    }

    protected open suspend fun reduceInit(pipeline: Pipeline<U, I, A>) {}

    protected open suspend fun reduceLifecycle(
        pipeline: Pipeline<U, I, A>,
        event: Lifecycle.Event,
    ) {
    }

    protected open suspend fun reduceBack(pipeline: Pipeline<U, I, A>) {
        action(pipeline, provideCommonAction(CommonAction.Back()))
    }
}

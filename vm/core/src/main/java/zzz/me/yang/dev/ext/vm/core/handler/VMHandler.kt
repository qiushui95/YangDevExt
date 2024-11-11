package zzz.me.yang.dev.ext.vm.core.handler

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.koin.core.Koin
import zzz.me.yang.dev.ext.vm.core.Pipeline
import zzz.me.yang.dev.ext.vm.core.action.CommonAction
import zzz.me.yang.dev.ext.vm.core.action.VMAction
import zzz.me.yang.dev.ext.vm.core.args.BasePageArgs
import zzz.me.yang.dev.ext.vm.core.async.Async
import zzz.me.yang.dev.ext.vm.core.intent.CommonIntent
import zzz.me.yang.dev.ext.vm.core.intent.PagingIntent
import zzz.me.yang.dev.ext.vm.core.intent.VMIntent
import zzz.me.yang.dev.ext.vm.core.ui.VMUI
import zzz.me.yang.dev.ext.vm.core.work.WorkStrategy

public interface VMHandler<U : VMUI, I, A : VMAction, Args>
    where I : VMIntent<U, I, A, Args>, Args : BasePageArgs {
    public fun getKoin(): Koin

    public fun getIoScope(): CoroutineScope

    public fun getArgs(): Args

    public fun provideCommonIntent(info: CommonIntent): I

    public fun provideCommonAction(info: CommonAction): A

    public fun providePagingIntent(info: PagingIntent): I

    public suspend fun handleCommonIntent(pipeline: Pipeline<U, I, A>, intent: CommonIntent)

    public suspend fun handlePagingIntent(pipeline: Pipeline<U, I, A>, intent: PagingIntent)

    public suspend fun <T> startSuspendWork(
        canContinue: U.() -> Boolean,
        asyncMapper: U.(Async) -> U,
        interval: Long = 0L,
        startMapper: (U.() -> U)? = null,
        dataMapper: (U.(T) -> U)? = null,
        failMapper: (U.(Throwable) -> U)? = null,
        endMapper: (U.() -> U)? = null,
        onStart: (suspend () -> Unit)? = null,
        onStart2: (suspend () -> Unit)? = null,
        onFail: (suspend (Throwable) -> Unit)? = null,
        onFail2: (suspend (Throwable) -> Unit)? = null,
        onSuccess: (suspend (T) -> Unit)? = null,
        onSuccess2: (suspend (T) -> Unit)? = null,
        onEnd: (suspend () -> Unit)? = null,
        block: suspend (U) -> T,
    ): Job?

    public suspend fun <T> startSuspendWork(
        key: String,
        workStrategy: WorkStrategy = WorkStrategy.CancelCurrent,
        interval: Long = 0L,
        startMapper: (U.() -> U)? = null,
        dataMapper: (U.(T) -> U)? = null,
        failMapper: (U.(Throwable) -> U)? = null,
        endMapper: (U.() -> U)? = null,
        onStart: (suspend () -> Unit)? = null,
        onStart2: (suspend () -> Unit)? = null,
        onFail: (suspend (Throwable) -> Unit)? = null,
        onFail2: (suspend (Throwable) -> Unit)? = null,
        onSuccess: (suspend (T) -> Unit)? = null,
        onSuccess2: (suspend (T) -> Unit)? = null,
        onEnd: (suspend () -> Unit)? = null,
        block: suspend (U) -> T,
    ): Job?

    public fun intent(pipeline: Pipeline<U, I, A>, intent: I)

    public fun intentCommon(pipeline: Pipeline<U, I, A>, info: CommonIntent)

    public fun intentInit(pipeline: Pipeline<U, I, A>)

    public fun intentError(pipeline: Pipeline<U, I, A>, error: Throwable)

    public fun intentPaging(pipeline: Pipeline<U, I, A>, info: PagingIntent)

    public fun intentPagingRefresh(pipeline: Pipeline<U, I, A>)

    public suspend fun action(pipeline: Pipeline<U, I, A>, action: A)

    public suspend fun actionCommon(pipeline: Pipeline<U, I, A>, info: CommonAction)

    public suspend fun actionToast(pipeline: Pipeline<U, I, A>, res: Int?, isShort: Boolean = true)

    public suspend fun actionToast(
        pipeline: Pipeline<U, I, A>,
        text: String?,
        isShort: Boolean = true,
    )

    public suspend fun actionBack(pipeline: Pipeline<U, I, A>, res: Int? = null)
}

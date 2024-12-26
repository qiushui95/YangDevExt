package zzz.me.yang.dev.ext.vm.core.handler

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.koin.core.Koin
import zzz.me.yang.dev.ext.vm.core.Pipeline
import zzz.me.yang.dev.ext.vm.core.action.CommonAction
import zzz.me.yang.dev.ext.vm.core.action.VMAction
import zzz.me.yang.dev.ext.vm.core.args.BasePageArgs
import zzz.me.yang.dev.ext.vm.core.intent.CommonIntent
import zzz.me.yang.dev.ext.vm.core.intent.PagingIntent
import zzz.me.yang.dev.ext.vm.core.intent.VMIntent
import zzz.me.yang.dev.ext.vm.core.ui.VMUI
import zzz.me.yang.dev.ext.vm.core.work.WorkAsync
import zzz.me.yang.dev.ext.vm.core.work.WorkStrategy

public interface VMHandler<U : VMUI, I, A : VMAction, Args>
    where I : VMIntent<U, I, A, Args>, Args : BasePageArgs {
    public fun getKoin(): Koin

    public fun getIoScope(): CoroutineScope

    public fun getArgs(): Args

    public fun provideCommonIntent(info: CommonIntent): I

    public fun provideCommonAction(info: CommonAction): A

    public suspend fun <R> withStateResult(block: suspend U.() -> R): R

    public suspend fun handleCommonIntent(pipeline: Pipeline<U, I, A>, intent: CommonIntent)

    public suspend fun <T> startSuspendWork(
        canContinue: U.() -> Boolean,
        asyncMapper: U.(WorkAsync) -> U,
        startMapper: (U.() -> U)? = null,
        successMapper: (U.(T) -> U)? = null,
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
        startMapper: (U.() -> U)? = null,
        successMapper: (U.(T) -> U)? = null,
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

    public fun intent(vararg intent: I?)

    public fun intentCommon(info: CommonIntent?)

    public fun intentInit()

    public fun intentError(error: Throwable?)

    public fun intentPaging(info: PagingIntent?)

    public fun intentPagingRefresh()

    public fun action(pipeline: Pipeline<U, I, A>, vararg action: A?)

    public fun actionCommon(pipeline: Pipeline<U, I, A>, info: CommonAction?)

    public fun getToastAction(res: Int?, isShort: Boolean = true): A?

    public fun actionToast(pipeline: Pipeline<U, I, A>, res: Int?, isShort: Boolean = true)

    public fun getToastAction(text: String?, isShort: Boolean = true): A?

    public fun actionToast(pipeline: Pipeline<U, I, A>, text: String?, isShort: Boolean = true)

    public fun getBackAction(res: Int? = null): A?

    public fun actionBack(pipeline: Pipeline<U, I, A>, res: Int? = null)

    public fun getPageAction(args: BasePageArgs?): A?

    public fun actionPage(pipeline: Pipeline<U, I, A>, args: BasePageArgs?)
}

package zzz.me.yang.dev.ext.vm.core.handler

import kotlinx.coroutines.CoroutineScope
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

    public suspend fun handleCommonIntent(pipeline: Pipeline<U, I, A>, intent: CommonIntent)

    public suspend fun <T> startSuspendWork(
        pipeline: Pipeline<U, I, A>,
        key: String? = null,
        workStrategy: WorkStrategy = WorkStrategy.CancelCurrent,
        canContinue: U.() -> Boolean = { true },
        asyncMapper: U.(WorkAsync) -> U = { this },
        startMapper: (U.() -> U)? = null,
        successMapper: (U.(T) -> U)? = null,
        failMapper: (U.(Throwable) -> U)? = null,
        endMapper: (U.() -> U)? = null,
        onStart: (suspend U.() -> Unit)? = null,
        onStart2: (suspend U.() -> Unit)? = null,
        onFail: (suspend U.(Throwable) -> Unit)? = null,
        onFail2: (suspend U.(Throwable) -> Unit)? = null,
        onSuccess: (suspend U.(T) -> Unit)? = null,
        onSuccess2: (suspend U.(T) -> Unit)? = null,
        onEnd: (suspend U.() -> Unit)? = null,
        onEnd2: (suspend U.() -> Unit)? = null,
        block: suspend (U) -> T,
    )

    public fun intent(vararg intent: I?)

    public fun intentCommon(info: CommonIntent?)

    public fun intentInit()

    public fun intentError(error: Throwable?)

    public fun intentPaging(info: PagingIntent?)

    public fun intentPagingRefresh(fromUI: Boolean = false, initPagingData: Boolean = false)

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

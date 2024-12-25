package zzz.me.yang.dev.ext.vm.core.intent

import pro.respawn.flowmvi.api.MVIIntent
import zzz.me.yang.dev.ext.vm.core.Pipeline
import zzz.me.yang.dev.ext.vm.core.action.VMAction
import zzz.me.yang.dev.ext.vm.core.args.BasePageArgs
import zzz.me.yang.dev.ext.vm.core.handler.VMHandler
import zzz.me.yang.dev.ext.vm.core.interval.IntervalWork
import zzz.me.yang.dev.ext.vm.core.interval.IntervalWorkGetter
import zzz.me.yang.dev.ext.vm.core.ui.VMUI

public abstract class VMIntent<U : VMUI, I, A : VMAction, Args> :
    MVIIntent,
    IntervalWork,
    IntervalWorkGetter where I : VMIntent<U, I, A, Args>, Args : BasePageArgs {
    override fun getIntervalKey(): String {
        return "Intent_${javaClass.name}"
    }

    override fun getInterval(): Long = 1_000L

    override fun getIntervalWork(): IntervalWork = this

    public abstract suspend fun VMHandler<U, I, A, Args>.reduce(pipeline: Pipeline<U, I, A>)
}

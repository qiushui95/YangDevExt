package zzz.me.yang.dev.ext.vm.core.intent

import pro.respawn.flowmvi.api.MVIIntent
import zzz.me.yang.dev.ext.vm.core.Pipeline
import zzz.me.yang.dev.ext.vm.core.action.VMAction
import zzz.me.yang.dev.ext.vm.core.args.BasePageArgs
import zzz.me.yang.dev.ext.vm.core.handler.VMHandler
import zzz.me.yang.dev.ext.vm.core.ui.VMUI

public interface VMIntent<U : VMUI, I, A : VMAction, Args> : MVIIntent
    where I : VMIntent<U, I, A, Args>, Args : BasePageArgs {
    public suspend fun VMHandler<U, I, A, Args>.reduce(pipeline: Pipeline<U, I, A>)
}

package zzz.me.yang.dev.ext.vm.ext

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import pro.respawn.flowmvi.api.SubscriberLifecycle
import pro.respawn.flowmvi.api.SubscriptionMode
import pro.respawn.flowmvi.compose.dsl.DefaultLifecycle
import pro.respawn.flowmvi.compose.dsl.subscribe
import zzz.me.yang.dev.ext.vm.core.BaseViewModel
import zzz.me.yang.dev.ext.vm.core.action.VMAction
import zzz.me.yang.dev.ext.vm.core.args.BasePageArgs
import zzz.me.yang.dev.ext.vm.core.intent.VMIntent
import zzz.me.yang.dev.ext.vm.core.ui.VMUI

public fun <U : VMUI, I, A : VMAction, Args : BasePageArgs> BaseViewModel<U, I, A, Args>.subscribe(
    lifecycleOwner: LifecycleOwner,
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    consume: suspend (action: A) -> Unit,
): Job where I : VMIntent<U, I, A, Args> {

    val consumeBlock: suspend (A) -> Unit = {
        runCatching { consume(it) }.onFailure { intentError(it) }
    }

    return lifecycleOwner.subscribe(store, consumeBlock, {}, lifecycleState)
}

@Composable
public fun <U : VMUI, I, A : VMAction, Args : BasePageArgs> BaseViewModel<U, I, A, Args>.subscribe(
    lifecycle: SubscriberLifecycle = DefaultLifecycle,
    mode: SubscriptionMode = SubscriptionMode.Started,
    consume: suspend CoroutineScope.(action: A) -> Unit = {},
): State<U> where I : VMIntent<U, I, A, Args> {

    val consumeBlock: suspend CoroutineScope.(A) -> Unit = {
        runCatching { consume(it) }.onFailure { intentError(it) }
    }

    return store.subscribe(lifecycle = lifecycle, mode = mode, consume = consumeBlock)
}

package zzz.me.yang.dev.ext.vm.core.action

import androidx.lifecycle.LifecycleOwner
import pro.respawn.flowmvi.api.MVIAction

public abstract class VMAction : MVIAction {
    public inline fun <reified T> castOwner(owner: LifecycleOwner?): T? {
        if (owner !is T) return null

        return owner
    }

    public abstract suspend operator fun invoke(owner: LifecycleOwner?)
}

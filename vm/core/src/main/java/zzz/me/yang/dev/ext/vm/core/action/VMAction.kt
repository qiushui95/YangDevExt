package zzz.me.yang.dev.ext.vm.core.action

import androidx.lifecycle.LifecycleOwner
import pro.respawn.flowmvi.api.MVIAction
import zzz.me.yang.dev.ext.vm.core.interval.IntervalWork
import zzz.me.yang.dev.ext.vm.core.interval.IntervalWorkGetter

public abstract class VMAction : MVIAction, IntervalWork, IntervalWorkGetter {
    override fun getIntervalKey(): String {
        return "Action_${javaClass.name}"
    }

    override fun getInterval(): Long {
        return 0
    }

    override fun getIntervalWork(): IntervalWork = this

    public inline fun <reified T> castOwner(owner: LifecycleOwner?): T? {
        if (owner !is T) return null

        return owner
    }

    public abstract suspend operator fun invoke(owner: LifecycleOwner?)
}

package zzz.me.yang.dev.ext.vm.core.intent

import androidx.lifecycle.Lifecycle
import zzz.me.yang.dev.ext.vm.core.interval.IntervalWork
import zzz.me.yang.dev.ext.vm.core.now

private typealias Event = Lifecycle.Event

public sealed class CommonIntent : IntervalWork {
    override fun getIntervalKey(): String {
        return "Intent_Common_${javaClass.name}"
    }

    override fun getInterval(): Long {
        return 1_000L
    }

    public data class OnError(val error: Throwable) : CommonIntent() {
        override fun getInterval(): Long {
            return 0L
        }
    }

    public data class OnLifecycle(val event: Event, val timestamp: Long = now()) : CommonIntent() {
        override fun getIntervalKey(): String {
            return "Intent_Common_Lifecycle_${event.javaClass.name}"
        }

        override fun getInterval(): Long {
            return 50L
        }
    }

    public data class OnInit(val timestamp: Long = now()) : CommonIntent()

    public data class OnBackPressed(val timestamp: Long = now()) : CommonIntent()

    public data class OnPaging(val pagingIntent: PagingIntent) : CommonIntent()
}

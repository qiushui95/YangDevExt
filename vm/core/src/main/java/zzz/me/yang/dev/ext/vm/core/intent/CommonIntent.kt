package zzz.me.yang.dev.ext.vm.core.intent

import androidx.lifecycle.Lifecycle
import zzz.me.yang.dev.ext.vm.core.interval.IntervalWork
import zzz.me.yang.dev.ext.vm.core.now

private typealias Event = Lifecycle.Event

public sealed class CommonIntent : IntervalWork {
    public data class OnError(val error: Throwable) : CommonIntent() {
        override fun getIntervalKey(): String {
            return "Intent_Common_Error"
        }

        override fun getInterval(): Long {
            return 0L
        }
    }

    public data class OnLifecycle(val event: Event, val timestamp: Long = now()) : CommonIntent() {
        override fun getIntervalKey(): String {
            return "Intent_Common_Lifecycle_${event.name}"
        }

        override fun getInterval(): Long {
            return 50L
        }
    }

    public data class OnInit(val timestamp: Long = now()) : CommonIntent() {
        override fun getIntervalKey(): String {
            return "Intent_Common_Init"
        }

        override fun getInterval(): Long {
            return 500L
        }
    }

    public data class OnBackPressed(val timestamp: Long = now()) : CommonIntent() {
        override fun getIntervalKey(): String {
            return "Intent_Common_Back"
        }

        override fun getInterval(): Long {
            return 500L
        }
    }

    public data class OnPaging(val pagingIntent: PagingIntent) : CommonIntent() {
        override fun getIntervalKey(): String {
            return "Intent_Common_Paging"
        }

        override fun getInterval(): Long {
            return 100L
        }
    }
}

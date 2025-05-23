package zzz.me.yang.dev.ext.vm.core.intent

import androidx.lifecycle.Lifecycle
import zzz.me.yang.dev.ext.vm.core.interval.IntervalWork
import zzz.me.yang.dev.ext.vm.core.now

private typealias Event = Lifecycle.Event
private typealias Intent = CommonIntent

public sealed class CommonIntent : IntervalWork {
    public data class OnError(val error: Throwable) : Intent() {
        override fun getIntervalKey(): String {
            return "Common_Intent_On_Error"
        }

        override fun getInterval(): Long {
            return 0L
        }
    }

    public data class OnLifecycle(val event: Event, val timestamp: Long = now()) : Intent() {
        override fun getIntervalKey(): String {
            return "Common_Intent_On_Lifecycle"
        }

        override fun getInterval(): Long = 0
    }

    public data class TrackLifecycle(val event: Event, val timestamp: Long = now()) : Intent() {
        override fun getIntervalKey(): String {
            return "Common_Intent_Track_Lifecycle"
        }

        override fun getInterval(): Long = 0
    }

    public data class OnInit(val timestamp: Long = now()) : Intent() {
        override fun getIntervalKey(): String {
            return "Common_Intent_Init"
        }

        override fun getInterval(): Long {
            return 500L
        }
    }

    public data class OnBackPressed(val timestamp: Long = now()) : Intent() {
        override fun getIntervalKey(): String {
            return "Common_Intent_Back"
        }

        override fun getInterval(): Long {
            return 500L
        }
    }

    public data class OnPaging(val pagingIntent: PagingIntent) : Intent() {
        override fun getIntervalKey(): String {
            return "Common_Intent_Paging"
        }

        override fun getInterval(): Long {
            return 100L
        }
    }
}

package zzz.me.yang.dev.ext.vm.core.intent

import androidx.lifecycle.Lifecycle
import zzz.me.yang.dev.ext.vm.core.now

private typealias Event = Lifecycle.Event

public sealed class CommonIntent {
    public data class OnError(val error: Throwable) : CommonIntent()

    public data class OnLifecycle(val event: Event, val timestamp: Long = now()) : CommonIntent()

    public data class OnInit(val timestamp: Long = now()) : CommonIntent()

    public data class OnBackPressed(val timestamp: Long = now()) : CommonIntent()

    public data class OnPaging(val pagingIntent: PagingIntent) : CommonIntent()
}

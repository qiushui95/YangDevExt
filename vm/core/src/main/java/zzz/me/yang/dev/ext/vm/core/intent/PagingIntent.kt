package zzz.me.yang.dev.ext.vm.core.intent

import zzz.me.yang.dev.ext.vm.core.now

public sealed class PagingIntent {
    public data class LoadInit(val timestamp: Long = now()) : PagingIntent()

    public data class Refresh(val timestamp: Long = now()) : PagingIntent()

    public data class LoadNextPage(val timestamp: Long = now()) : PagingIntent()
}

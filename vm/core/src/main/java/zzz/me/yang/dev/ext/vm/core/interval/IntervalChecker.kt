package zzz.me.yang.dev.ext.vm.core.interval

internal interface IntervalChecker {
    suspend fun checkInterval(workGetter: IntervalWorkGetter, next: suspend () -> Unit)
}

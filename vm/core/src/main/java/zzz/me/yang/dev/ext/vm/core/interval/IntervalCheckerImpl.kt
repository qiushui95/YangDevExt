package zzz.me.yang.dev.ext.vm.core.interval

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class IntervalCheckerImpl : IntervalChecker {
    private val intervalMap = mutableMapOf<String, Long>()

    private val mutex = Mutex()

    override suspend fun checkInterval(
        workGetter: IntervalWorkGetter,
        next: suspend () -> Unit,
    ) = mutex.withLock {
        startCheckInterval(workGetter.getIntervalWork(), next)
    }

    private suspend fun startCheckInterval(work: IntervalWork, next: suspend () -> Unit) {
        val interval = work.getInterval()

        if (interval <= 0) return

        val key = work.getIntervalKey()

        val lastTime = intervalMap[key] ?: 0

        val currentTime = System.currentTimeMillis()

        if (lastTime + interval <= currentTime) return

        intervalMap[key] = currentTime

        next()
    }
}

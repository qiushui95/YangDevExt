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
        if (getCanNext(work).not()) return

        val intervalKey = work.getIntervalKey()
        val interval = work.getInterval()

        if (interval > 0) intervalMap[intervalKey] = System.currentTimeMillis()

        next()
    }

    private fun getCanNext(work: IntervalWork): Boolean {
        val interval = work.getInterval()

        if (interval <= 0) return true

        val key = work.getIntervalKey()

        val currentTime = System.currentTimeMillis()

        val lastTime = intervalMap[key] ?: return true

        return lastTime + interval < currentTime
    }
}

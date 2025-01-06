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

    private fun log(work: IntervalWork, messageBlock: () -> String) {
        if (work.getCanLog().not()) return

        work.log(messageBlock())
    }

    private fun getCanNext(work: IntervalWork): Boolean {
        val interval = work.getInterval()

        if (interval <= 0) {
            log(work) { "${work.getIntervalKey()} do not need interval check" }
            return true
        }

        val key = work.getIntervalKey()

        val currentTime = System.currentTimeMillis()

        val lastTime = intervalMap[key]

        if (lastTime == null) {
            log(work) { "${work.getIntervalKey()} lastTime is null" }

            return true
        }

        val canNext = lastTime + interval < currentTime

        if (canNext) {
            log(work) { "${work.getIntervalKey()} can next" }
        } else {
            log(work) {
                val sb = StringBuilder()
                sb.append(work.getIntervalKey())
                sb.append(" can not next, last run time is ")
                sb.append(lastTime)
                sb.append(", current time is ")
                sb.append(currentTime)
                sb.append(", interval is ")
                sb.append(interval)
                sb.append(", next can run time is ")
                sb.append(lastTime + interval)
                sb.toString()
            }
        }

        return canNext
    }
}

package zzz.me.yang.dev.ext.vm.core.work

import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class WorkStrategyCheckerImpl : WorkStrategyChecker {
    private val jobMap by lazy { mutableMapOf<String, Job>() }

    private val mutex = Mutex()

    override suspend fun startCheck(
        workStrategy: WorkStrategy,
        key: String,
        next: suspend (Job) -> Job,
    ): Job? = mutex.withLock { check(workStrategy, key, next) }

    private suspend fun check(
        workStrategy: WorkStrategy,
        key: String,
        next: suspend (Job) -> Job,
    ): Job? {
        val workJob = jobMap.getOrPut(key) { SupervisorJob() }

        return checkWorkStatus(workJob, workStrategy) { next(it) }
    }

    private suspend fun checkWorkStatus(
        workJob: Job,
        workStrategy: WorkStrategy,
        next: suspend (Job) -> Job,
    ): Job? {
        val jobList = workJob.children

        val hasWorkingJob = jobList.any { it.isActive }

        when {
            workStrategy is WorkStrategy.CancelCurrent && hasWorkingJob -> return null
            workStrategy is WorkStrategy.CancelBefore -> {
                jobList.forEach { it.cancelAndJoin() }
            }

            workStrategy is WorkStrategy.WaitIdle -> {
                jobList.forEach { it.join() }
            }

            else -> {}
        }

        return next(workJob)
    }
}

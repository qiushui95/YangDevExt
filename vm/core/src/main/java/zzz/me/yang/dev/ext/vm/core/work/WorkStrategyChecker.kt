package zzz.me.yang.dev.ext.vm.core.work

import kotlinx.coroutines.Job

public interface WorkStrategyChecker {

    public suspend fun startCheck(
        workStrategy: WorkStrategy,
        key: String,
        next: suspend (Job) -> Job
    ): Job?
}
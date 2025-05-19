package zzz.me.yang.dev.ext.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

public class PriorityTaskQueue(
    private val scope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val logger: (String) -> Unit = {},
) {
    public sealed class Priority(internal val sort: Int) {
        public data object High : Priority(1)

        public data object Normal : Priority(2)

        public data object Low : Priority(3)
    }

    private data class TaskInfo(
        val taskId: String,
        val priority: Priority,
        val task: suspend () -> Unit,
    )

    private val mutex = Mutex()
    private val taskList = mutableListOf<TaskInfo>()

    private suspend fun <R> doWithTaskList(block: MutableList<TaskInfo>.() -> R): R {
        return mutex.withLock { block(taskList) }
    }

    public fun addTask(taskId: String, priority: Priority, task: suspend () -> Unit) {
        scope.launch(Dispatchers.Default) {
            doWithTaskList {
                add(TaskInfo(taskId, priority, task))
                sortBy { it.priority.sort }
            }

            processNextTask()
        }
    }

    private val workingJob by lazy { SupervisorJob() }

    public fun processNextTask() {
        for (childJob in workingJob.children) {
            if (childJob.isActive) return
        }

        scope.launch(dispatcher + workingJob + CoroutineExceptionHandler { _, _ -> }) {
            tryNextTask(this)
        }
    }

    private suspend fun tryNextTask(scope: CoroutineScope) {
        val hasMoreTask = startNextTask(scope)

        if (hasMoreTask) tryNextTask(scope)
    }

    private val taskExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
    }

    private suspend fun startNextTask(scope: CoroutineScope): Boolean {
        val taskInfo = doWithTaskList {
            logger("start task list size: ${map { it.taskId }}")
            runCatching { removeAt(0) }.getOrNull()
        }

        logger("start task ${taskInfo?.taskId}")

        taskInfo ?: return false

        scope.launch(SupervisorJob() + taskExceptionHandler) {
            taskInfo.task()
        }.join()

        return true
    }
}

package zzz.me.yang.dev.ext.coroutines.priority

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private typealias TaskInfoList = MutableList<PriorityTaskQueue.TaskInfo>
private typealias Priority = PriorityTaskPriority
private typealias Strategy = PriorityTaskStrategy

public class PriorityTaskQueue(
    private val scope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val logger: (String) -> Unit = {},
) {
    internal data class TaskInfo(
        val taskId: String,
        val priority: Priority,
        val task: suspend () -> Unit,
    )

    private val mutex = Mutex()
    private val taskList = mutableListOf<TaskInfo>()

    private suspend fun <R> doWithTaskList(block: MutableList<TaskInfo>.() -> R): R {
        return mutex.withLock { block(taskList) }
    }

    public fun addTask(
        taskId: String,
        priority: Priority,
        strategy: Strategy,
        task: suspend () -> Unit,
    ) {
        scope.launch(Dispatchers.Default) {
            doWithTaskList { this.addTask(TaskInfo(taskId, priority, task), strategy) }

            processNextTask()
        }
    }

    private fun TaskInfoList.addTask(taskInfo: TaskInfo, strategy: Strategy) {
        val sameIdList = filter { it.taskId == taskInfo.taskId }

        when (strategy) {
            PriorityTaskStrategy.NoCheck -> {}
            PriorityTaskStrategy.Replace -> removeAll(sameIdList)
            PriorityTaskStrategy.Skip -> return
        }

        add(taskInfo)

        sortBy { it.priority.sort }
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

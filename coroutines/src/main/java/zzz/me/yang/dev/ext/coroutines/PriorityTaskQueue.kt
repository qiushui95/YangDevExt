package zzz.me.yang.dev.ext.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private typealias TaskInfoList = MutableList<PriorityTaskQueue.TaskInfo>

public class PriorityTaskQueue(
    private val scope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val logger: (String) -> Unit = {},
) {
    public sealed class Priority(internal val sort: Int) {
        public data object Max : Priority(0)

        public data object High : Priority(Int.MAX_VALUE / 4)

        public data object Normal : Priority(Int.MAX_VALUE / 3)

        public data object Low : Priority(Int.MAX_VALUE / 2)

        public data object Min : Priority(Int.MAX_VALUE)

        public class Custom(sort: Int) : Priority(sort) {
            init {
                require(sort > 0 && sort < Int.MAX_VALUE) {
                    "Custom priority sort must in range (0,Int.MAX_VALUE)"
                }
            }
        }
    }

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

    public fun addTask(taskId: String, priority: Priority, task: suspend () -> Unit) {
        scope.launch(Dispatchers.Default) {
            doWithTaskList { this.addTask(taskId, priority, task) }

            processNextTask()
        }
    }

    private fun TaskInfoList.addTask(taskId: String, priority: Priority, task: suspend () -> Unit) {
        for (taskInfo in this) {
            if (taskInfo.taskId == taskId) return
        }

        add(TaskInfo(taskId, priority, task))

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

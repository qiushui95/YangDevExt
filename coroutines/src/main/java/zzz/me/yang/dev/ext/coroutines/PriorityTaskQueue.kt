package zzz.me.yang.dev.ext.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

public class PriorityTaskQueue(
    private val scope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    public sealed class Priority(internal val sort: Int) {
        public data object High : Priority(1)

        public data object Normal : Priority(2)

        public data object Low : Priority(3)
    }

    private data class TaskInfo(val priority: Priority, val task: suspend () -> Unit)

    private val mutex = Mutex()
    private val taskList = mutableListOf<TaskInfo>()

    private suspend fun <R> doWithTaskList(block: (MutableList<TaskInfo>) -> R): R {
        return mutex.withLock { block(taskList) }
    }

    public fun addTask(priority: Priority, task: suspend () -> Unit) {
        scope.launch(Dispatchers.Default) {
            doWithTaskList {
                taskList.add(TaskInfo(priority, task))
                taskList.sortBy { it.priority.sort }
            }
        }

        processNextTask()
    }

    private val processMutex = Mutex()

    public fun processNextTask() {
        scope.launch(dispatcher) {
            tryNextTask(this)
        }
    }

    private suspend fun tryNextTask(scope: CoroutineScope) {
        if (processMutex.isLocked) return

        processMutex.lock()

        while (scope.isActive) {
            val hasMoreTask = startNextTask(scope)

            if (hasMoreTask.not()) break
        }

        processMutex.unlock()
    }

    private val taskExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
    }

    private suspend fun startNextTask(scope: CoroutineScope): Boolean {
        val taskInfo = doWithTaskList {
            taskList.runCatching { removeFirst() }
        }.getOrNull() ?: return false

        scope.launch(SupervisorJob() + taskExceptionHandler) {
            taskInfo.task()
        }.join()

        return true
    }
}

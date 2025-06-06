package zzz.me.yang.dev.ext.coroutines

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.joda.time.DateTime
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import zzz.me.yang.dev.ext.coroutines.priority.PriorityTaskQueue

internal class PriorityTaskQueueTest {
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private fun log(msg: String) {
        val now = DateTime.now().toString("HH:mm:ss:SSS")

        println("$now->[${Thread.currentThread().name}]->$msg")
    }

    private fun startTest(scope: TestScope, throwError: Boolean) {
        println("--------------------测试开始($$throwError)--------------------")

        val queue = PriorityTaskQueue(scope)

//        scope.launch(Dispatchers.IO) {
//            repeat(3) {
//                delay(800)
//                log("新增High任务$it")
//                queue.addTask(PriorityTaskQueue.Priority.High) {
//                    log("执行High任务$it")
//                    delay(1000)
//                    log("结束High任务$it")
//                }
//            }
//        }
//
//        scope.launch(Dispatchers.IO) {
//            repeat(4) {
//                delay(400)
//                log("新增Normal任务$it")
//                queue.addTask(PriorityTaskQueue.Priority.Normal) {
//                    log("执行Normal任务$it")
//                    delay(500)
//                    if (throwError && it == 2) {
//                        throw RuntimeException("测试异常")
//                    }
//                    log("结束Normal任务$it")
//                }
//            }
//        }

        scope.launch(Dispatchers.IO) {
            repeat(5) {
                delay(100)
                log("新增Low任务$it")
                queue.addTask(PriorityTaskQueue.Priority.Low) {
                    log("执行Low任务$it")
                    delay(300)
                    log("结束Low任务$it")
                }
            }
        }
    }

    @Disabled
    @Test
    fun testSimple() = testScope.runTest {
        startTest(testScope, false)

        // 等待任务执行完毕
        advanceUntilIdle()
    }

//    @Disabled
    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun testQueue(throwError: Boolean) = testScope.runTest {
        startTest(testScope, throwError)

        // 等待任务执行完毕
        advanceUntilIdle()
    }
}

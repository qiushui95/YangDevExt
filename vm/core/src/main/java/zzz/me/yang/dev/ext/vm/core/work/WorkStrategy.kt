package zzz.me.yang.dev.ext.vm.core.work

/**
 * 加载数据策略
 */
public sealed class WorkStrategy {
    /**
     * 不检测前一个状态
     */
    public data object NoCheck : WorkStrategy()

    /**
     * 等待之前的任务结束
     */
    public data object WaitIdle : WorkStrategy()

    /**
     * 取消之前的任务
     */
    public data object CancelBefore : WorkStrategy()

    /**
     * 取消当前
     */
    public data object CancelCurrent : WorkStrategy()
}

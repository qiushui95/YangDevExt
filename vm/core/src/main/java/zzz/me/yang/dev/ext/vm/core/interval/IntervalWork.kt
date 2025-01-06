package zzz.me.yang.dev.ext.vm.core.interval

public interface IntervalWork {
    public fun getIntervalKey(): String

    public fun getInterval(): Long

    public fun getCanLog(): Boolean = false

    public fun log(message: String) {}
}

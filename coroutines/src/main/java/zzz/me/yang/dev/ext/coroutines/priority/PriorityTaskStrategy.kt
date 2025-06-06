package zzz.me.yang.dev.ext.coroutines.priority

public sealed class PriorityTaskStrategy {
    public data object NoCheck : PriorityTaskStrategy()

    public data object Skip : PriorityTaskStrategy()

    public data object Replace : PriorityTaskStrategy()
}

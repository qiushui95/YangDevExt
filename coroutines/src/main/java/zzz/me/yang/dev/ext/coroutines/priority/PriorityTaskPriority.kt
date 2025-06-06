package zzz.me.yang.dev.ext.coroutines.priority

public sealed class PriorityTaskPriority(internal val sort: Int) {
    public data object Max : PriorityTaskPriority(0)

    public data object High : PriorityTaskPriority(Int.MAX_VALUE / 4)

    public data object Normal : PriorityTaskPriority(Int.MAX_VALUE / 3)

    public data object Low : PriorityTaskPriority(Int.MAX_VALUE / 2)

    public data object Min : PriorityTaskPriority(Int.MAX_VALUE)

    public class Custom(sort: Int) : PriorityTaskPriority(sort) {
        init {
            require(sort > 0 && sort < Int.MAX_VALUE) {
                "Custom priority sort must in range (0,Int.MAX_VALUE)"
            }
        }
    }
}

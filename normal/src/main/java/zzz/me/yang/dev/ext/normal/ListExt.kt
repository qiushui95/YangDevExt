package zzz.me.yang.dev.ext.normal

public fun <T, R> List<T>.mapSelf(mapper: T.() -> R): List<R> = map { it.mapper() }

public inline fun <T, R, C : MutableCollection<in R>> Iterable<T>.mapSelfTo(
    destination: C,
    transform: T.() -> R,
): MutableCollection<in R> {
    return mapTo(destination) { it.transform() }
}

public fun <T, R> List<T>.mapNotNullSelf(mapper: T.() -> R?): List<R> = mapNotNull { it.mapper() }

public fun <T> List<T>.equalsContent(other: List<T>): Boolean = when {
    size != other.size -> false
    !containsAll(other) -> false
    !other.containsAll(this) -> false
    else -> true
}

public fun List<String>.filterNotBlank(): List<String> = this.filterNot { it.isBlank() }

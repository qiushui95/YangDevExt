package zzz.me.yang.dev.ext.normal

public fun String.padSide(length: Int, padChar: Char = ' ', padStartFirst: Boolean = true): String {
    if (this.length >= length) return this

    val isSameLength = (length - this.length) % 2 == 0

    val startPadLength = if (isSameLength) {
        (length - this.length) / 2
    } else if (padStartFirst) {
        (length - this.length) / 2 + 1
    } else {
        (length - this.length) / 2
    }

    val endPadLength = length - this.length - startPadLength

    return padStart(startPadLength, padChar).padEnd(endPadLength, padChar)
}

public fun String.ellipsis(length: Int): String {
    if (this.length <= length) return this

    return this.substring(0, length - 3) + "..."
}

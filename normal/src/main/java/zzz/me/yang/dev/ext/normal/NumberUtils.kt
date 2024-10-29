package zzz.me.yang.dev.ext.normal

public object NumberUtils {
    public fun getProgress(cur: Long, total: Long): Float {
        if (total <= 0) return 0f

        if (cur <= 0) return 0f

        if (cur >= total) return 1f

        return cur * 1f / total
    }

    public fun getPercent(cur: Long, total: Long): Float {
        return getProgress(cur, total) * 100
    }

    public fun formatNumStr(
        num: Double,
        maxDecimalSize: Int,
        removeSuffix: Boolean = true,
    ): String {
        val decimalSize = maxOf(0, maxDecimalSize)

        val result = num.toString()

        val dotIndex = result.indexOf('.')

        if (dotIndex == -1) return result

        val endIndex = (dotIndex + decimalSize + 1).coerceIn(dotIndex, result.length)

        return result.substring(0, endIndex)
            .removeSuffix0(removeSuffix)
    }

    private fun String.removeSuffix0(removeSuffix: Boolean): String {
        if (removeSuffix.not()) return this

        return removeSuffix("0")
            .removeSuffix(".")
    }

    private fun getNumStr(num: Long, num2: Long, maxDecimalSize: Int): String {
        return formatNumStr(num * 1.0 / num2, maxDecimalSize)
    }

    public fun shortNum2Str(num: Int, maxDecimalSize: Int = 2): String {
        return shortNum2Str(num.toLong(), maxDecimalSize)
    }

    public fun shortNum2Str(num: Long, maxDecimalSize: Int = 2): String {
        val numStr: String
        val postfix: String

        when {
            num >= 10000 -> {
                numStr = getNumStr(num, 10000, maxDecimalSize)
                postfix = "w"
            }

            num >= 1000 -> {
                numStr = getNumStr(num, 1000, maxDecimalSize)
                postfix = "k"
            }

            else -> {
                numStr = num.toString()
                postfix = ""
            }
        }

        return numStr + postfix
    }
}

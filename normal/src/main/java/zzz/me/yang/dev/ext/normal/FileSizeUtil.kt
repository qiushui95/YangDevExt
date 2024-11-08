package zzz.me.yang.dev.ext.normal


public object FileSizeUtil {
    public sealed class Symbol(internal val maxSize: Long) {
        public data object KB : Symbol(1024L * 1024)

        public data object MB : Symbol(1024L * 1024 * 1024)

        public data object GB : Symbol(1024L * 1024 * 1024 * 1024)
    }

    /**
     * 格式化文件大小
     * @param maxSymbol 最大的单位
     * @param maxDecimalSize 最大的小数位数
     * @param removeSuffix 是否移除后缀
     */
    public fun format(
        size: Long,
        maxSymbol: Symbol = Symbol.MB,
        maxDecimalSize: Int = 2,
        removeSuffix: Boolean = true,
    ): String {
        var curSize = size.toDouble()
        var curMaxSize = maxSymbol.maxSize.toDouble()

        var step = 0

        while (curSize > 1024.0 && curMaxSize > 1024.0) {
            curSize /= 1024.0
            curMaxSize /= 1024.0

            step++
        }

        val result = NumberUtils.formatNumStr(curSize, maxDecimalSize, removeSuffix)

        return when (step) {
            0 -> "${result}B"
            1 -> result + "KB"
            2 -> result + "MB"
            else -> result + "GB"
        }
    }
}

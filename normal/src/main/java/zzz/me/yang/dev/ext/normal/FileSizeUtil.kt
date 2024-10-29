package zzz.me.yang.dev.ext.normal

import kotlin.math.min

public object FileSizeUtil {
    public sealed class Symbol(internal val maxSize: Long) {
        public data object KB : Symbol(1024L * 1024)

        public data object MB : Symbol(1024L * 1024 * 1024)

        public data object GB : Symbol(1024L * 1024 * 1024 * 1024)
    }

    /**
     * 格式化文件大小
     * @param maxSymbol 最大的单位
     */
    public fun format(size: Long, maxSymbol: Symbol = Symbol.MB): String {
        var curSize = size.toDouble()
        var curMaxSize = maxSymbol.maxSize.toDouble()

        var step = 0

        while (curSize > 1024.0 && curMaxSize > 1024.0) {
            curSize /= 1024.0
            curMaxSize /= 1024.0

            step++
        }

        return when (step) {
            0 -> "${size}B"
            1 -> subNumToStr(curSize, 0) + "KB"
            2 -> subNumToStr(curSize, 0) + "MB"
            else -> subNumToStr(curSize, 2) + "GB"
        }
    }

    private fun subNumToStr(num: Double, decimalSize: Int): String {
        val numStr = num.toString()

        val dotIndex = numStr.indexOf(".")

        if (dotIndex == -1) {
            return numStr
        }

        val endIndex = if (decimalSize == 0) {
            dotIndex
        } else {
            min(numStr.length, dotIndex + decimalSize + 1)
        }

        return numStr.substring(0, endIndex)
    }
}

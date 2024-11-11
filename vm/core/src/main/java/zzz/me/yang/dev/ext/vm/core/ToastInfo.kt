package zzz.me.yang.dev.ext.vm.core

import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils

public sealed class ToastInfo {
    public abstract val isShort: Boolean

    public fun showToast() {
        if (isShort) {
            ToastUtils.showShort(getToastText())
        } else {
            ToastUtils.showLong(getToastText())
        }
    }

    public abstract fun getToastText(): String

    public data class Res(
        val resId: Int,
        override val isShort: Boolean = true,
        val args: Array<Any> = emptyArray(),
    ) : ToastInfo() {
        override fun getToastText(): String {
            return if (args.isEmpty()) {
                StringUtils.getString(resId)
            } else {
                StringUtils.getString(resId, *args)
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Res

            if (resId != other.resId) return false
            if (isShort != other.isShort) return false
            if (!args.contentEquals(other.args)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = resId
            result = 31 * result + isShort.hashCode()
            result = 31 * result + args.contentHashCode()
            return result
        }
    }

    public data class Text(
        val text: String,
        override val isShort: Boolean = true,
    ) : ToastInfo() {
        override fun getToastText(): String {
            return text
        }
    }
}

package zzz.me.yang.dev.ext.utils

import android.widget.Toast
import androidx.annotation.StringRes

public object ToastUtils {
    private fun show(text: CharSequence, duration: Int) {
        Toast.makeText(Utils.getApp(), text, duration).show()
    }

    public fun showShort(@StringRes resId: Int, vararg args: Any) {
        show(StringUtils.getString(resId, *args), Toast.LENGTH_SHORT)
    }

    public fun showShort(text: CharSequence) {
        show(text, Toast.LENGTH_SHORT)
    }

    public fun showLong(@StringRes resId: Int, vararg args: Any) {
        show(StringUtils.getString(resId, *args), Toast.LENGTH_LONG)
    }

    public fun showLong(text: CharSequence) {
        show(text, Toast.LENGTH_LONG)
    }
}

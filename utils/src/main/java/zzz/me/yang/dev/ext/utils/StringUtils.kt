package zzz.me.yang.dev.ext.utils

import androidx.annotation.StringRes

public object StringUtils {
    public fun getString(@StringRes id: Int, vararg formatArgs: Any): String {
        return Utils.getApp().getString(id, *formatArgs)
    }
}

package zzz.me.yang.dev.ext.utils

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

@SuppressLint("StaticFieldLeak")
public object Utils {
    private var context: Context? = null

    public fun init(context: Context) {
        this.context = context.applicationContext
    }

    public fun getApp(): Application {
        return context as? Application
            ?: throw IllegalStateException("Utils is not initialized or context is not Application")
    }
}

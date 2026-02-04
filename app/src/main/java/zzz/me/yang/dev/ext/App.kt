package zzz.me.yang.dev.ext

import android.app.Application
import zzz.me.yang.dev.ext.utils.Utils

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Utils.init(this)
    }
}

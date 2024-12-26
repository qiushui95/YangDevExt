package zzz.me.yang.dev.ext.vm.core.action

import zzz.me.yang.dev.ext.vm.core.ToastInfo
import zzz.me.yang.dev.ext.vm.core.args.BasePageArgs
import zzz.me.yang.dev.ext.vm.core.interval.IntervalWork
import zzz.me.yang.dev.ext.vm.core.now

public sealed class CommonAction : IntervalWork {
    override fun getIntervalKey(): String {
        return "Action_Common_${javaClass.simpleName}"
    }

    override fun getInterval(): Long {
        return 0L
    }

    public data class Error(val error: Throwable) : CommonAction()

    public data class Toast(val toastInfo: ToastInfo) : CommonAction()

    public data class Back(val timestamp: Long = now(), val toastInfo: ToastInfo?) : CommonAction()

    public data class LaunchPage(val args: BasePageArgs) : CommonAction() {
        override fun getIntervalKey(): String {
            return "Action_Common_LaunchPage_${args.javaClass.name}"
        }

        override fun getInterval(): Long {
            return 500
        }
    }
}

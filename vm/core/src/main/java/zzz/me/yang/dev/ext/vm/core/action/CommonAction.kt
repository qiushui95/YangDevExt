package zzz.me.yang.dev.ext.vm.core.action

import zzz.me.yang.dev.ext.vm.core.ToastInfo
import zzz.me.yang.dev.ext.vm.core.args.BasePageArgs
import zzz.me.yang.dev.ext.vm.core.now

public sealed class CommonAction {
    public data class Error(val error: Throwable) : CommonAction()

    public data class Toast(val toastInfo: ToastInfo) : CommonAction()

    public data class Back(val timestamp: Long = now()) : CommonAction()

    public data class LaunchPage(val args: BasePageArgs) : CommonAction()
}

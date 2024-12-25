package zzz.me.yang.dev.ext.vm.core.work

public sealed class WorkAsync(
    public val isLoading: Boolean,
    public val isSuccess: Boolean,
    public val isFail: Boolean,
    public val isComplete: Boolean,
    public val shouldLoad: Boolean,
    internal val sort: Int,
) {
    public val isIdle: Boolean = isLoading.not()

    public data object Uninitialized : WorkAsync(
        isLoading = false,
        isSuccess = false,
        isFail = false,
        isComplete = false,
        shouldLoad = true,
        sort = 0,
    )

    public data object Loading : WorkAsync(
        isLoading = true,
        isSuccess = false,
        isFail = false,
        isComplete = false,
        shouldLoad = false,
        sort = 1,
    )

    public data class Fail(val error: Throwable) : WorkAsync(
        isLoading = false,
        isSuccess = false,
        isFail = true,
        isComplete = true,
        shouldLoad = true,
        sort = 2,
    )

    public data object Success : WorkAsync(
        isLoading = false,
        isSuccess = true,
        isFail = false,
        isComplete = true,
        shouldLoad = false,
        sort = 3,
    )
}

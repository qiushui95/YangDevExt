package zzz.me.yang.dev.ext.fragment

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner

public object FragmentResultUtils {
    public const val REQUEST_KEY: String = "fragmentResultRequestKey"
    public const val KEY_RESULT: String = "fragmentResult"

    public fun setResult(
        owner: LifecycleOwner?,
        fragmentManagerBlock: Fragment.() -> FragmentManager = { parentFragmentManager },
        bundleBlock: Bundle.() -> Unit = {},
        resultBlock: (() -> Parcelable)? = null,
    ) {
        if (owner !is Fragment) throw RuntimeException("owner is not Fragment")

        val manager = fragmentManagerBlock(owner)

        val result = Bundle()

        result.bundleBlock()

        result.putParcelable(KEY_RESULT, resultBlock?.invoke())

        manager.setFragmentResult(REQUEST_KEY, result)
    }

    public inline fun <reified T : Parcelable> getParcelable(bundle: Bundle, key: String): T? {
        return if (Build.VERSION.SDK_INT >= 34) {
            bundle.getParcelable(key, T::class.java)
        } else {
            @Suppress("DEPRECATION")
            bundle.getParcelable(key)
        }
    }

    public inline fun <reified T : Parcelable> getResult(bundle: Bundle): T? {
        return getParcelable(bundle, KEY_RESULT)
    }

    public inline fun <reified T : Parcelable> getResultNotNull(bundle: Bundle): T {
        return getResult<T>(bundle) ?: throw NullPointerException()
    }

    public inline fun <reified T : Parcelable> setListener(
        manager: FragmentManager,
        owner: LifecycleOwner,
        crossinline listener: (T?) -> Unit,
    ) {
        manager.setFragmentResultListener(REQUEST_KEY, owner) { _, bundle ->
            listener(getResult<T>(bundle))
        }
    }

    public inline fun <reified T : Parcelable> setListener(
        fragment: Fragment,
        crossinline listener: (T?) -> Unit,
    ) {
        setListener<T>(fragment.childFragmentManager, fragment, listener)
    }

    public inline fun <reified T : Parcelable> setListener(
        activity: FragmentActivity,
        crossinline listener: (T?) -> Unit,
    ) {
        setListener<T>(activity.supportFragmentManager, activity, listener)
    }
}

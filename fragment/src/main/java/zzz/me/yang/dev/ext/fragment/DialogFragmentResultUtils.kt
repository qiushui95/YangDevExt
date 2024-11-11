package zzz.me.yang.dev.ext.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner

public object DialogFragmentResultUtils {

    private const val KEY_SHOW_TAG = "showTag"

    private const val EVENT_ON_SHOW = "onShow"
    private const val EVENT_ON_DISMISS = "onDismiss"

    public fun getShowTag(bundle: Bundle): String? {
        return bundle.getString(KEY_SHOW_TAG)
    }

    private fun setResult(
        owner: LifecycleOwner?,
        requestKey: String,
        showTag: String,
        fragmentManagerBlock: Fragment.() -> FragmentManager,
    ) {

        if (owner !is Fragment) throw RuntimeException("owner is not Fragment")

        val manager = fragmentManagerBlock(owner)

        val result = Bundle()

        result.putString(KEY_SHOW_TAG, showTag)

        manager.setFragmentResult(requestKey, result)
    }

    public fun setOnShowResult(
        owner: LifecycleOwner?,
        showTag: String,
        fragmentManagerBlock: Fragment.() -> FragmentManager = { parentFragmentManager },
    ) {
        setResult(owner, EVENT_ON_SHOW, showTag, fragmentManagerBlock)
    }

    public fun setOnDismissResult(
        owner: LifecycleOwner?,
        showTag: String,
        fragmentManagerBlock: Fragment.() -> FragmentManager = { parentFragmentManager },
    ) {
        setResult(owner, EVENT_ON_DISMISS, showTag, fragmentManagerBlock)
    }

    private fun setListener(
        requestKey: String,
        manager: FragmentManager,
        owner: LifecycleOwner,
        listener: (showTag: String) -> Unit,
    ) {
        manager.setFragmentResultListener(requestKey, owner) { _, bundle ->
            getShowTag(bundle)?.apply(listener)
        }
    }

    public fun setShowListener(
        manager: FragmentManager,
        owner: LifecycleOwner,
        listener: (showTag: String) -> Unit,
    ) {
        setListener(EVENT_ON_SHOW, manager, owner, listener)
    }

    public fun setDismissListener(
        manager: FragmentManager,
        owner: LifecycleOwner,
        listener: (showTag: String) -> Unit,
    ) {
        setListener(EVENT_ON_DISMISS, manager, owner, listener)
    }
}

package zzz.me.yang.dev.ext.fragment

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

private typealias ListenerMap = MutableMap<String, MutableSet<FragmentResultListener>>

public object FragmentResultUtils {
    public const val REQUEST_KEY: String = "fragmentResultRequestKey"
    public const val KEY_RESULT: String = "fragmentResult"

    public fun setResult(
        manager: FragmentManager,
        requestKey: String = REQUEST_KEY,
        bundleBlock: Bundle.() -> Unit = {},
        resultBlock: () -> Parcelable,
    ) {
        val result = Bundle()

        result.bundleBlock()

        result.putParcelable(KEY_RESULT, resultBlock())

        manager.setFragmentResult(requestKey, result)
    }

    public fun setResult(
        owner: LifecycleOwner?,
        fragmentManagerBlock: Fragment.() -> FragmentManager = { parentFragmentManager },
        requestKey: String = REQUEST_KEY,
        bundleBlock: Bundle.() -> Unit = {},
        resultBlock: () -> Parcelable,
    ) {
        if (owner !is Fragment) throw RuntimeException("owner is not Fragment")

        val manager = fragmentManagerBlock(owner)

        setResult(manager, requestKey, bundleBlock, resultBlock)
    }

    public inline fun <reified T : Parcelable> getParcelable(bundle: Bundle, key: String): T? {
        return if (Build.VERSION.SDK_INT >= 34) {
            bundle.getParcelable(key, T::class.java)
        } else {
            @Suppress("DEPRECATION")
            bundle.getParcelable(key)
        }
    }

    public fun getResult(bundle: Bundle): Parcelable? {
        return getParcelable(bundle, KEY_RESULT)
    }

    private val listenerMap by lazy { mutableMapOf<String, MutableSet<FragmentResultListener>>() }

    @Synchronized
    private fun doWithListenerMap(block: (ListenerMap) -> Unit) {
        block(listenerMap)
    }

    private fun getListenerKey(
        manager: FragmentManager,
        owner: LifecycleOwner,
        requestKey: String,
    ): String {
        val managerId = System.identityHashCode(manager)
        val ownerId = System.identityHashCode(owner)

        return "${requestKey}_${managerId}_$ownerId"
    }

    private val lifecycleObserver by lazy {
        object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                val ownerId = "_${System.identityHashCode(owner)}_"

                doWithListenerMap { map ->
                    for (key in map.keys) {
                        if (ownerId !in key) continue

                        map.remove(key)
                    }

                    val iterator = map.iterator()

                    while (iterator.hasNext()) {
                        val (key, _) = iterator.next()

                        if (ownerId !in key) continue

                        iterator.remove()
                    }
                }
            }
        }
    }

    public fun removeListener(listener: FragmentResultListener) {
        doWithListenerMap { map ->
            val iterator = map.iterator()

            while (iterator.hasNext()) {
                val (_, set) = iterator.next()

                set.remove(listener)

                if (set.isNotEmpty()) continue

                iterator.remove()
            }
        }
    }

    public fun setListener(
        manager: FragmentManager,
        owner: LifecycleOwner,
        requestKey: String = REQUEST_KEY,
        listener: FragmentResultListener,
    ) {
        val listenerKey = getListenerKey(manager, owner, requestKey)

        doWithListenerMap { it.getOrPut(listenerKey) { mutableSetOf() }.add(listener) }

        owner.lifecycle.addObserver(lifecycleObserver)

        manager.setFragmentResultListener(requestKey, owner) { _, bundle ->
            doWithListenerMap { map -> map[listenerKey]?.forEach { it.onReceive(bundle) } }
        }
    }

    public fun setListener(
        fragment: Fragment,
        requestKey: String = REQUEST_KEY,
        listener: FragmentResultListener,
    ) {
        setListener(fragment.childFragmentManager, fragment, requestKey, listener)
    }

    public fun setListener(
        activity: FragmentActivity,
        requestKey: String = REQUEST_KEY,
        listener: FragmentResultListener,
    ) {
        setListener(activity.supportFragmentManager, activity, requestKey, listener)
    }
}

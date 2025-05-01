package io.github.yangentao.xutil

import io.github.yangentao.types.printX
import java.lang.ref.WeakReference

fun main() {
    val m = WeakMap<String, Int>(map = HashMap())
    m["a"] = 123
    printX(m["a"])
}

class WeakMap<K : Any, V : Any>(capacity: Int = 16, val map: MutableMap<K, WeakReference<V>> = HashMap(capacity)) : MutableMap<K, V> {

    inner class Entry(override val key: K, override var value: V) : MutableMap.MutableEntry<K, V> {
        override fun setValue(newValue: V): V {
            val old = value
            value = newValue
            put(key, newValue)
            return old
        }
    }

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() {
            val ls = LinkedHashSet<MutableMap.MutableEntry<K, V>>()
            for (e in map.entries) {
                val v = e.value.get()
                if (v != null) {
                    ls += Entry(e.key, v)
                }
            }
            return ls

        }
    override val keys: MutableSet<K> = map.keys

    override val values: MutableCollection<V> = map.values.map { it.get() }.filterNotNull().toMutableList()

    override fun containsKey(key: K): Boolean = map.containsKey(key)

    override fun containsValue(value: V): Boolean {
        return map.values.any { it.get() === value }
    }

    override fun clear() = map.clear()

    override fun put(key: K, value: V): V? = map.put(key, WeakReference(value))?.get()

    override fun putAll(from: Map<out K, V>) {
        for ((k, v) in from) {
            map[k] = WeakReference(v)
        }
    }

    override val size: Int = map.size

    override fun remove(key: K): V? {
        return map.remove(key)?.get()
    }

    override operator fun get(key: K): V? {
        return map[key]?.get()
    }

    override fun isEmpty(): Boolean {
        return map.isEmpty()
    }

    operator fun set(key: K, value: V) {
        map[key] = WeakReference(value)
    }

    @Suppress("unused")
    fun tryClean() {
        for (e in map.entries) {
            if (e.value.get() == null) {
                map.remove(e.key)
            }
        }
    }
}

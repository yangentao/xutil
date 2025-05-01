@file:Suppress("unused")

package io.github.yangentao.xutil

import java.lang.ref.WeakReference
import java.sql.Connection
import java.sql.Date
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty

//------
typealias DateSQL = Date
typealias DateUtil = java.util.Date
typealias Predicater<T> = (T) -> Boolean

interface WithConnection {
    val connection: Connection
}

@Suppress("UNCHECKED_CAST")
class FunctionProperty<V : Any>(val block: (KFunction<*>) -> V) {

    operator fun getValue(thisRef: KFunction<*>, property: KProperty<*>): V {
        val key = "$thisRef/$property"
        return map.getOrPut(key) { block(thisRef) } as V
    }

    companion object {
        val map = HashMap<String, Any>()
    }
}

@Suppress("UNCHECKED_CAST")
class ClassProperty<T : Any>(val block: (KClass<*>) -> T) {

    operator fun getValue(thisRef: KClass<*>, property: KProperty<*>): T {
        val key = "$thisRef/$property"
        return map.getOrPut(key) { block(thisRef) } as T
    }

    companion object {
        val map = HashMap<String, Any>()
    }
}

@Suppress("UNCHECKED_CAST")
class PropertyProperty<T : Any>(val block: (KProperty<*>) -> T) {

    operator fun getValue(thisRef: KProperty<*>, property: KProperty<*>): T {
        val key = "$thisRef/$property"
        return map.getOrPut(key) { block(thisRef) } as T
    }

    companion object {
        val map = java.util.HashMap<String, Any>()
    }
}

class WeakRef<T>(value: T?) {
    private var w: WeakReference<T>? = null
    var value: T?
        get() = w?.get()
        set(value) {
            w = if (value == null) null else WeakReference(value)
        }

    init {
        this.value = value
    }

    val isNull: Boolean get() = w?.get() == null

    override fun equals(other: Any?): Boolean {
        if (other is WeakRef<*>) {
            return w?.get() === other.w?.get()
        }
        return false
    }

    override fun hashCode(): Int {
        return w?.get()?.hashCode() ?: 0
    }
}



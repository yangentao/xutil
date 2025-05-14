@file:Suppress("MemberVisibilityCanBePrivate")

package io.github.yangentao.xutil

import io.github.yangentao.types.*
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

fun printX(vararg vs: Any?) {
    val s = vs.joinToString(" ") {
        it?.toString() ?: "null"
    }
    println(s)
}

val Thread.isMain: Boolean get() = this.id == 1L

data class LabelValue<T : Any>(val label: String, val value: T)
//fun <T:Any> String.on(value:T): LabelValue<T> {
//    return LabelValue(this, value)
//}

interface NotifyKey {
    val notifyKey: String
}

class ResultNotify<R : Any>(private val shotOnce: Boolean = true) {
    private val lockMap = ConcurrentHashMap<String, SimpleLock<R>>()

    fun notifyAll(notifyKey: NotifyKey, result: R?) {
        notifyAll(notifyKey.notifyKey, result)
    }

    fun notifyAll(key: String, result: R?) {
        val lock = if (shotOnce) lockMap.remove(key) else lockMap[key]
        lock?.notifyAll(result)
    }

    fun wait(notifyKey: NotifyKey, timeoutMS: Long = 10_000): R? {
        return wait(notifyKey.notifyKey, timeoutMS)
    }

    fun wait(key: String, timeoutMS: Long = 10_000): R? {
        val lock: SimpleLock<R> = lockMap.getOrPut(key) { SimpleLock() }
        val r: R? = lock.wait(timeoutMS)
        if (r == null && shotOnce) lockMap.remove(key)
        return r
    }
}

class SimpleLock<T> {
    private val lock = java.lang.Object()
    private var result: T? = null

    fun wait(ms: Long): T? {
        synchronized(lock) {
            try {
                lock.wait(ms)
            } catch (_: Throwable) {
            }
        }
        return result
    }

    fun notify(result: T?) {
        synchronized(lock) {
            this.result = result
            lock.notify()
        }
    }

    fun notifyAll(result: T?) {
        synchronized(lock) {
            this.result = result
            lock.notifyAll()
        }
    }
}

class MapCache<K : Any, V : Any>(private val finder: (K) -> V?) {
    private val map = HashMap<K, V>()
    private val nullSet = HashSet<K>()

    fun get(key: K): V? {
        if (nullSet.contains(key)) return null
        if (map.containsKey(key)) return map[key]
        val v = finder(key)
        if (v == null) {
            nullSet.add(key)
        } else {
            map[key] = v
        }
        return v
    }
}

typealias LongStringCache = ValueCache<Long, String>

class ValueCache<K : Any, V : Any>(private val finder: (K) -> V?) {
    private val map = HashMap<K, V>()
    private val nullSet = HashSet<K>()

    operator fun get(key: K): V? {
        if (nullSet.contains(key)) return null
        return map.getOrPutX(key) {
            val v = finder(key)
            if (v == null) nullSet.add(key)
            v
        }
    }
}

class LongCache<T : Any>(private val finder: (Long) -> T?) {
    private val map = HashMap<Long, T?>()
    private val nullSet = HashSet<Long>()

    fun get(key: Long): T? {
        if (nullSet.contains(key)) return null
        return map.getOrPutX(key) {
            val v = finder(key)
            if (v == null) nullSet.add(key)
            v
        }
    }
}

class ItemCache<T : Any>(private val finder: (Int) -> T?) {
    private val map = HashMap<Int, T?>()
    private val nullSet = HashSet<Int>()

    fun get(key: Int): T? {
        if (nullSet.contains(key)) return null
        return map.getOrPutX(key) {
            val v = finder(key)
            if (v == null) nullSet.add(key)
            v
        }
    }
}

inline fun <K, V> MutableMap<K, V>.getOrPutX(key: K, defaultValue: () -> V?): V? {
    val value = get(key)
    return if (value == null) {
        val answer = defaultValue()
        if (answer != null) put(key, answer)
        answer
    } else {
        value
    }
}

val UUID.hexText: String get() = String.format("%x%x", this.mostSignificantBits, this.leastSignificantBits)

fun dateDisplay(v: Any, format: String): String {
    //java.util.Date包含java.sql.Date和Timestamp,Time
    return when (v) {
        is java.util.Date -> SimpleDateFormat(format, Locale.getDefault()).format(v)
        is Long -> SimpleDateFormat(format, Locale.getDefault()).format(java.util.Date(v))
        is DateTime -> v.format(format)
        is LocalDate -> v.format(format)
        is LocalDateTime -> v.format(format)
        is LocalTime -> v.format(format)
        else -> v.toString()
    }
}

val Int.fileSize: String get() = this.toLong().fileSize

val Long.fileSize: String
    get() {
        return when {
            this > GB -> (this * 1.0 / GB).maxFraction(2) + "G"
            this > MB -> (this * 1.0 / MB).maxFraction(2) + "M"
            this > KB -> (this * 1.0 / KB).maxFraction(2) + "K"
            else -> this.toString() + "字节"
        }
    }

//fun main() {
//    println(joinPath("/a/", "b/", "/c", "/d/", "e", "/", ""))
//    println(joinPath("c:\\", "b/", "/c", "/d/", "e", "/", ""))
//}

fun KClass<*>.resourceBytes(name: String): ByteArray? {
    val i = this.java.classLoader.getResourceAsStream(name) ?: return null
    i.use {
        return it.readBytes()
    }
}

fun KClass<*>.resourceText(name: String): String? {
    val i = this.java.classLoader.getResourceAsStream(name) ?: return null
    i.use {
        return it.readBytes().toString(Charsets.UTF_8)
    }
}

fun File.ensureDirs(): File {
    if (!this.exists()) {
        this.mkdirs()
    }
    return this
}

//mill seconds
class IntervalRun(private val interval: Long, initTime: Long = System.currentTimeMillis()) {
    private var lastTime: Long = initTime
    fun run(block: () -> Unit): Boolean {
        val tm = System.currentTimeMillis()
        if (tm - lastTime < interval) return false
        lastTime = tm
        block()
        return true
    }
}

fun shellExec(s: String): Pair<Int, List<String>> {
    val p = Runtime.getRuntime().exec(s)
    p.waitFor()
    val n = p.exitValue()
    val ls = p.inputStream.bufferedReader().readLines()
    return n to ls
}
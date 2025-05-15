@file:Suppress("MemberVisibilityCanBePrivate")

package io.github.yangentao.xutil

import java.util.concurrent.ConcurrentHashMap


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
@file:Suppress("unused")

package io.github.yangentao.xutil

import io.github.yangentao.types.printX
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock

fun main() {
    testSync()
    Thread.sleep(100)
    println("END")
}

private fun testSync() {
    val lock = SyncObject()
    var count = 0
    for (i in 0..<100) {
        async {
            sync(lock) {
                count += 1
                printX(count, i, Thread.currentThread().id)
            }
        }
    }
}

private fun testPromise() {
    val p: XPromise<Int> = async {
        Thread.sleep(1000)
        123
    }.onDone {
        printX("done")
    }.onResult {
        printX("onResult:", it)
    }
    printX("Start")
    p.wait()
    p.wait()
    val v = p.get()
    printX("get: ", v)

}

fun <T> await(block: () -> T): T {
    return async(block).get()
}

fun <T> async(block: () -> T): XPromise<T> {
    val promise = XPromise<T>()
    val task = AsyncTask<T>(promise, block)
    runThreadTask(task)
    return promise
}

fun asyncTask(block: () -> Unit) {
    runThreadTask(block)
}

private class AsyncTask<T>(val promise: XPromise<T>, val block: () -> T) : Runnable {
    var cancelled = AtomicBoolean(false)
    var running = AtomicBoolean(false)

    init {
        promise.doCancel = {
            cancelled.set(true)
            !running.get()
        }
    }

    override fun run() {
        if (cancelled.get()) return
        running.set(true)
        try {
            val v = block.invoke()
            promise.setResult(v)
        } catch (e: Throwable) {
            promise.setError(e)
        } finally {
            running.set(false)
        }
    }
}

fun sync(obj: SyncObject, block: () -> Unit) {
    obj.syncBlock(block)
}

class SyncObject {
    private val lock = ReentrantLock()

    fun syncBlock(block: () -> Unit) {
        lock.lock()
        try {
            block()
        } finally {
            lock.unlock()
        }
    }
}

private val jver: Int = System.getProperty("java.specification.version")?.toIntOrNull() ?: 0

@Suppress("Since15")
fun runThreadTask(task: Runnable) {
    if (jver >= 21) {
        Thread.ofVirtual().start(task)
    } else {
        Thread(task).also { it.isDaemon = true }.start()
    }
}

@Suppress("Since15", "DEPRECATION")
val Thread.tid: Long get() = if (jver > 19) this.threadId() else this.id
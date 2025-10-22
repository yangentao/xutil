@file:Suppress("unused")

package io.github.yangentao.xutil

import io.github.yangentao.types.asyncTask
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock

fun <T> async(block: () -> T): XPromise<T> {
    val task = AsyncTask<T>(block)
    asyncTask(task)
    return task.promise
}

fun sync(obj: SyncLock, block: Runnable) {
    obj.syncRun(block)
}

class SyncLock {
    private val lock = ReentrantLock()

    fun syncRun(block: Runnable) {
        lock.lock()
        try {
            block.run()
        } finally {
            lock.unlock()
        }
    }
}

private class AsyncTask<T>(val block: () -> T) : Runnable {
    val promise: XPromise<T> = XPromise()
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



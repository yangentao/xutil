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
    private var running = AtomicBoolean(false)

    @Volatile
    private var thread: Thread? = null

    init {
        promise.doCancel = {
            if (running.get()) {
                thread?.interrupt()
            }
            running.set(false)
            true
        }
    }

    override fun run() {
        thread = Thread.currentThread()
        running.set(true)
        try {
            val v = block.invoke()
            promise.setResult(v)
        } catch (ie: InterruptedException) {
            promise.doCancel = null
        } catch (e: Throwable) {
            promise.setError(e)
        } finally {
            running.set(false)
            thread = null
            promise.doCancel = null
        }
    }
}



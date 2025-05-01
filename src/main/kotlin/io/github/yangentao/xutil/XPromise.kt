@file:Suppress("unused")

package io.github.yangentao.xutil

import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

//不能使用 synchronized
class XPromise<T> {
    private val sem = Semaphore(0, true)
    private var _done = AtomicBoolean(false)
    private var _canceled = AtomicBoolean(false)

    @Volatile
    private var _resulet: T? = null

    @Volatile
    private var _cause: Throwable? = null

    private var _onResult: ((T) -> Unit)? = null
    private var _onDone: ((XPromise<T>) -> Unit)? = null
    private var _onError: ((Throwable) -> Unit)? = null
    var doCancel: (() -> Boolean)? = null

    private fun unlock() {
        sem.release()
    }

    fun onResult(block: (T) -> Unit): XPromise<T> {
        if (isSuccess()) {
            block(get())
        } else {
            _onResult = block
        }
        return this
    }

    fun onDone(block: (XPromise<T>) -> Unit): XPromise<T> {
        if (isDone()) {
            block(this)
        } else {
            _onDone = block
        }
        return this
    }

    fun onError(block: (Throwable) -> Unit): XPromise<T> {
        if (isError()) {
            block(_cause!!)
        } else {
            _onError = block
        }
        return this
    }

    fun setResult(value: T) {
        if (isDone()) error("State already been done.")
        _done.set(true)
        _resulet = value
        unlock()

        runThreadTask {
            _onResult?.invoke(value)
            _onDone?.invoke(this)
        }
    }

    fun setError(cause: Throwable) {
        if (isDone()) error("State already been done.")
        _done.set(true)
        _cause = cause
        unlock()
        runThreadTask {
            _onError?.invoke(cause)
            _onDone?.invoke(this)
        }
    }

    fun cancel(): Boolean {
        if (isDone()) return false
        _done.set(true)
        _canceled.set(true)
        val b = doCancel?.invoke()
        unlock()
        runThreadTask {
            _onDone?.invoke(this)
        }
        return b != false
    }

    fun isDone(): Boolean {
        return _done.get()
    }

    fun isSuccess(): Boolean {
        return _done.get() && _cause == null && !_canceled.get()
    }

    fun isError(): Boolean {
        return _done.get() && _cause != null
    }

    fun isCancelled(): Boolean {
        return _done.get() && _canceled.get()
    }

    fun tryGet(): T? {
        if (isSuccess()) return _resulet
        return null
    }

    //T 可以是Optional, XPromise<String?>, 因此不能是: return _result!!
    @Suppress("UNCHECKED_CAST")
    fun get(milliseconds: Long = 0L): T {
        if (isDone()) {
            if (isSuccess()) {
                return _resulet as T
            } else {
                errorAsync("NOT success, $this", _cause)
            }
        }
        wait(milliseconds)
        if (isDone()) {
            if (isSuccess()) {
                return _resulet as T
            } else {
                errorAsync("NOT success, $this", _cause)
            }
        }
        errorAsync("Timeout: $this")
    }

    fun wait(): Boolean {
        return wait(0)
    }

    fun wait(milliseconds: Long): Boolean {
        return wait(milliseconds, TimeUnit.MILLISECONDS)
    }

    fun wait(timeout: Long, unit: TimeUnit): Boolean {
        if (isDone()) return true
        if (timeout <= 0) {
            sem.acquire()
        } else {
            sem.tryAcquire(timeout, unit)
        }
        return isDone()
    }

}

class AsyncException(msg: String, cause: Throwable? = null) : Exception(msg, cause)

fun errorAsync(msg: String, cause: Throwable? = null): Nothing {
    throw AsyncException(msg, cause)
}
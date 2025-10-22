@file:Suppress("unused")

package io.github.yangentao.xutil

import io.github.yangentao.types.FutureState
import io.github.yangentao.types.asyncTask
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

//不能使用 synchronized
class XPromise<T> {
    private val sem = Semaphore(0, true)
    private var _state = AtomicReference<FutureState>(FutureState.RUNNING)

    @Volatile
    private var _resulet: T? = null

    @Volatile
    private var _cause: Throwable? = null

    private var _onDone: ((XPromise<T>) -> Unit)? = null
    private var _onResult: ((T) -> Unit)? = null
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
        if (isDone()) return
        _state.set(FutureState.SUCCESS)
        _resulet = value
        unlock()

        asyncTask {
            _onResult?.invoke(value)
            _onDone?.invoke(this)
        }
    }

    fun setError(cause: Throwable) {
        if (isDone()) return
        _state.set(FutureState.FAILED)
        _cause = cause
        unlock()
        asyncTask {
            _onError?.invoke(cause)
            _onDone?.invoke(this)
        }
    }

    fun cancel(): Boolean {
        if (isDone()) return false
        _state.set(FutureState.CANCELLED)
        val b = doCancel?.invoke()
        unlock()
        asyncTask {
            _onDone?.invoke(this)
        }
        return b != false
    }

    fun isDone(): Boolean {
        return _state.get() != FutureState.RUNNING
    }

    fun isSuccess(): Boolean {
        return _state.get() == FutureState.SUCCESS
    }

    fun isError(): Boolean {
        return _state.get() == FutureState.FAILED
    }

    fun isCancelled(): Boolean {
        return _state.get() == FutureState.CANCELLED
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
            sem.release()
        } else {
            if (sem.tryAcquire(timeout, unit)) {
                sem.release()
            }
        }
        return isDone()
    }

}

class AsyncException(msg: String, cause: Throwable? = null) : Exception(msg, cause)

private fun errorAsync(msg: String, cause: Throwable? = null): Nothing {
    throw AsyncException(msg, cause)
}
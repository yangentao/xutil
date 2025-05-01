package io.github.yangentao.xutil

import java.io.PrintWriter
import java.io.StringWriter

inline fun <R> safe(block: () -> R): R? {
    try {
        return block()
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
    return null
}

val Throwable.stackInfo: String
    get() {
        val w = StringWriter(1024)
        val p = PrintWriter(w)
        this.printStackTrace(p)
        p.flush()
        return w.toString()
    }


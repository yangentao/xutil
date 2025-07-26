package io.github.yangentao.xutil

import io.github.yangentao.types.printX

/**
 * Created by entaoyang@163.com on 2017/4/27.
 */

class Tick(val prefix: String = "") {
    var from: Long = System.currentTimeMillis()
    var to: Long = 0L

    fun start() {
        from = System.currentTimeMillis()
    }

    fun end(msg: String = "") {
        to = System.currentTimeMillis()
        val delta = to - from
        from = to
        printX(prefix, msg, delta, "ms")
    }
}

inline fun tick(msg: String, block: () -> Unit) {
    val t = Tick()
    block()
    t.end(msg)
}
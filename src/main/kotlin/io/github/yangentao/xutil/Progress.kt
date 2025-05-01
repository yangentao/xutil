package io.github.yangentao.xutil

/**
 * Created by entaoyang@163.com on 2016/12/20.
 */

val PROGRESS_DELAY = 100

interface Progress {
    fun onStart(total: Int)

    fun onProgress(current: Int, total: Int, percent: Int)

    fun onFinish(success: Boolean)
}

class DelayProgress(private val wraped: Progress, private val delayMill: Long = 50) : Progress {
    private var fireTime: Long = 0
    private var progressTime: Long = 0

    private var lastTotal: Int = 0
    private var lastCurrent: Int = 0
    private var lastPercent: Int = 0

    override fun onStart(total: Int) {
        wraped.onStart(total)
    }

    override fun onProgress(current: Int, total: Int, percent: Int) {
        val tm = System.currentTimeMillis()
        progressTime = tm
        if (fireTime != 0L && tm - fireTime < delayMill) {
            lastCurrent = current
            lastPercent = percent
            lastTotal = total
            return
        }
        fireTime = tm
        wraped.onProgress(current, total, percent)
    }

    override fun onFinish(success: Boolean) {
        if (fireTime != progressTime) {
            fireTime = progressTime
            wraped.onProgress(lastCurrent, lastTotal, lastPercent)
        }
        wraped.onFinish(success)
    }

}
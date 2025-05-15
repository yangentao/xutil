package io.github.yangentao.xutil

/**
 * Created by entaoyang@163.com on 16/5/13.
 */



@Suppress("ReplaceSizeZeroCheckWithIsEmpty")
fun CharSequence?.blank(): Boolean {
    if (this == null) return true
    if (this.length == 0) return true
    if (this.trim().isEmpty()) return true
    return false
}

@Suppress("ReplaceSizeZeroCheckWithIsEmpty")
fun CharSequence?.trimed(): String? {
    if (this == null) return null
    if (this.length == 0) return ""
    return this.trim().toString()
}

@Suppress("ReplaceSizeZeroCheckWithIsEmpty")
fun CharSequence?.trimedOr(): String {
    if (this == null) return ""
    if (this.length == 0) return ""
    return this.trim().toString()
}


fun String.subAfter(delimiter: String, missingDelimiterValue: String? = this): String? {
    val index = indexOf(delimiter)
    return if (index == -1) missingDelimiterValue else substring(index + delimiter.length, length)
}

fun String.firstLine(): String {
    var i: Int = 0
    while (i < this.length) {
        if (this[i] == '\r' || this[i] == '\n') break
        i += 1
    }
    if (i == this.length) return this
    return this.substring(0, i)
}

infix fun String?.caseLessEQ(other: String?): Boolean {
    if (this == other) return true
    if (this == null || other == null) {
        return false
    }
    return this.compareTo(other, ignoreCase = true) == 0
}

infix fun String.start(other: String): Boolean {
    return this.startsWith(other)
}

infix operator fun String.times(n: Int): String {
    val sb = StringBuilder(this.length * n + 1)
    for (i in 0 until n) {
        sb.append(this)
    }
    return sb.toString()
}

fun String.matchIp4(): Boolean {
    return this.matches("""\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}""".toRegex())
}



//"""
//		fun ok(){
//			print("hello");
//		}
//"""
//==>
//"""fun ok(){
//	print("hello");
//}"""
fun String.trimColumns(tabSize: Int = 4): String {
    val lines = this.lines()
    var n = 1000
    for (line in lines) {
        if (line.trim().isEmpty()) {
            continue
        }
        var w = 0
        for (c in line) {
            if (c == ' ') {
                w += 1
            } else if (c == '\t') {
                w += tabSize
            } else {
                break
            }
        }
        n = kotlin.math.min(n, w)
    }
    val ls = ArrayList<String>()
    for (line in lines) {
        var w = 0
        var index = 0
        for (c in line) {
            if (w >= n) {
                break
            }
            ++index
            if (c == ' ') {
                w += 1
            } else if (c == '\t') {
                w += tabSize
            } else {
                break
            }
        }
        ls += line.substring(index)
    }
    return ls.joinToString("\n")
}



fun String?.hasCharLast(ch: Char): Boolean {
    return (this?.lastIndexOf(ch) ?: -1) >= 0
}

fun String?.hasChar(ch: Char): Boolean {
    return (this?.indexOf(ch) ?: -1) >= 0
}

//分隔成长度不大于n的字符串数组
fun String.truck(n: Int): List<String> {
    val ls = ArrayList<String>()
    if (this.length <= n) {
        ls.add(this)
    } else {
        val x = this.length / n
        val y = this.length % n
        for (i in 1..x) {
            val start = (i - 1) * n
            ls.add(this.substring(start, start + n))
        }
        if (y != 0) {
            ls.add(this.substring(x * n))
        }
    }
    return ls
}


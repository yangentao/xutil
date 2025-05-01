@file:Suppress("unused")

package io.github.yangentao.xutil

/**
 * Created by entaoyang@163.com on 16/5/13.
 */
object StringComparatorIgnoreCase : Comparator<String> {
    override fun compare(o1: String, o2: String): Int {
        return o1.compareTo(o2, ignoreCase = true)
    }
}

fun CharSequence?.empty(): Boolean {
    return this.isNullOrEmpty()
}

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

infix fun <T : Appendable> T.add(s: CharSequence?): T {
    if (s != null) this.append(s)
    return this
}

infix fun <T : Appendable> T.add(ch: Char): T {
    this.append(ch)
    return this
}

operator fun StringBuilder.plusAssign(s: String) {
    this.append(s)
}

operator fun StringBuilder.plusAssign(ch: Char) {
    this.append(ch)
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

fun String.replaceChars(vararg charValuePair: Pair<Char, String>): String {
    val sb = StringBuilder(this.length + 8)
    for (ch in this) {
        val p = charValuePair.find { it.first == ch }
        if (p != null) {
            sb.append(p.second)
        } else {
            sb.append(ch)
        }
    }
    return sb.toString()
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

fun String.substr(from: Int, size: Int): String {
    val a = if (from >= 0) {
        from
    } else 0
    val b = if (a + size < this.length) {
        a + size
    } else {
        this.length
    }
    return this.substring(a, b)
}

//"abcd=defg-123".substringBetween('=','-') => "defg"
//"abcd=defg=123".substringBetween('=','=') => "defg"
//"abcd==123".substringBetween('=','=') => ""
//"abcd=123".substringBetween('=','=') => null
fun String.substringBetween(a: Char, b: Char): String? {
    val nA = this.indexOf(a)
    if (nA >= 0) {
        val nB = this.indexOf(b, nA + 1)
        if (nB >= 0) {
            return this.substring(nA + 1, nB)
        }
    }
    return null
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

fun String.escapeHtml(): String {
    val sb = StringBuffer((this.length * 1.1).toInt())
    this.forEach {
        when (it) {
            '<' -> sb.append("&lt;")
            '>' -> sb.append("&gt;")
            '"' -> sb.append("&quot;")
            '\'' -> sb.append("&#x27;")
            '&' -> sb.append("&amp;")
            '/' -> sb.append("&#x2F;")
            else -> sb.append(it)
        }
    }
    return sb.toString()
}

//left: 左边界, 如果是空字符串或null, 表示开始位置
//right: 右边界, 如果是空字符串或null, 表示结束位置
fun String.substringBetween(left: String?, right: String?, startIndex: Int = 0, ignoreCase: Boolean = false): String? {
    if (left.isNullOrEmpty()) {
        if (right.isNullOrEmpty()) {
            return this
        } else {
            val n = this.indexOf(right, startIndex, ignoreCase)
            if (n >= 0) return this.substring(0, n)
            return null
        }
    } else {
        if (right.isNullOrEmpty()) {
            val n = this.indexOf(left, startIndex, ignoreCase)
            if (n >= 0) return this.substring(n + left.length)
            return null
        }
    }

    val nA = this.indexOf(left, startIndex, ignoreCase)
    if (nA >= 0) {
        val nB = this.indexOf(right, nA + left.length, ignoreCase)
        if (nB >= 0) {
            return this.substring(nA + left.length, nB)
        }
    }
    return null
}


@file:Suppress("KotlinConstantConditions", "unused")

package io.github.yangentao.xutil

inline fun <reified T> T?.ifNull(block: () -> Unit): T? {
    if (this == null) block()
    return this
}

inline fun <reified T : Any> T?.ifNotNull(block: (T) -> Unit): T? {
    if (this != null) block(this)
    return this
}

inline fun <reified T : Any> T.ifEqual(value: T?, block: (T) -> Unit): T {
    if (this == value) block(this)
    return this
}

inline fun <reified T : Any> T.ifNotEqual(value: T?, block: (T, T?) -> Unit): T {
    if (this != value) block(this, value)
    return this
}

inline fun <reified E, T : Collection<E>> T.ifEmpty(block: () -> Unit): T {
    if (this.isEmpty()) block()
    return this
}

inline fun <reified E, T : Collection<E>> T.ifNotEmpty(block: (Collection<E>) -> Unit): T {
    if (this.isNotEmpty()) block(this)
    return this
}

inline fun String?.ifNotEmpty(block: (String) -> Unit): String? {
    if (this != null && this.isNotEmpty()) block(this)
    return this
}

inline fun String?.ifEmpty(block: () -> Unit): String? {
    if (this == null || this.isEmpty()) block()
    return this
}

@Suppress("NOTHING_TO_INLINE", "ReplaceSizeCheckWithIsNotEmpty")
inline fun CharSequence?.isNotNullEmpty(): Boolean {
    return this != null && this.length > 0
}

infix fun String?.or(other: String): String {
    if (this.isNullOrEmpty()) return other
    return this
}

inline fun <reified T : Comparable<T>> T.ifGreat(v: T, block: (T) -> Unit): T {
    if (this > v) block(this)
    return this
}

inline fun <reified T : Comparable<T>> T.ifGreatEqual(v: T, block: (T) -> Unit): T {
    if (this >= v) block(this)
    return this
}

inline fun <reified T : Comparable<T>> T.greatEqual(v: T): T {
    if (this < v) return v
    return this
}

inline fun <reified T : Comparable<T>> T.lessEqual(v: T): T {
    if (this > v) return v
    return this
}

inline fun <reified T : Comparable<T>> T.onLE(v: T, newValue: T): T {
    if (this <= v) return newValue
    return this
}

inline fun <reified T : Comparable<T>> T.onGE(v: T, newValue: T): T {
    if (this >= v) return newValue
    return this
}

inline fun <reified T : Comparable<T>> T.onEQ(v: T, newValue: T): T {
    if (this == v) return newValue
    return this
}





package util

import io.github.yangentao.xutil.decodeValue
import io.github.yangentao.xutil.second
import kotlin.reflect.full.valueParameters
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class A {
    val age: Int = 0
    val name: String = ""
    val ls: List<Int> = emptyList()
    val map: Map<String, Int> = emptyMap()

    fun hello(a: Int, s: String) {}
}

class DecodeTest {

    @Suppress("UNCHECKED_CAST")
    @Test
    fun d1() {
        assertEquals(123, A::age.decodeValue("123"))
        assertEquals("999", A::name.decodeValue("999"))
        assertEquals(99, A::hello.valueParameters.first().decodeValue("99"))
        assertEquals("99", A::hello.valueParameters.second().decodeValue("99"))
        assertContentEquals(listOf(11, 22, 33), A::ls.decodeValue(listOf("11", "22", "33")) as? List<Int>)
        assertContentEquals(listOf(1, 2, 3), A::ls.decodeValue("1,2,3") as? List<Int>)

        val v = A::map.decodeValue(mapOf("a" to "1", "b" to "2"))
        assertTrue(v is Map<*, *>)
        val map: Map<String, Int> = v as Map<String, Int>
        assertEquals(1, map["a"])
        assertEquals(2, map["b"])

    }
}
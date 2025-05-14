package util

import io.github.yangentao.xutil.decodeValue
import io.github.yangentao.xutil.second
import kotlin.reflect.full.valueParameters
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

private class A {
    val age: Int = 0
    val name: String = ""
    val ls: List<Int> = emptyList()

    fun hello(a: Int, s: String) {}
}

class DecodeTest {

    @Test
    fun d1() {
        assertEquals(123, A::age.decodeValue("123"))
        assertEquals("999", A::name.decodeValue("999"))
        assertEquals(99, A::hello.valueParameters.first().decodeValue("99"))
        assertEquals("99", A::hello.valueParameters.second().decodeValue("99"))
        val a = A::ls.decodeValue(listOf("11", "22", "33"))
        println(a)
        println(a!!::class)
        println((a as List<*>).first()!!::class)
//        val ls:List<Int>? =  as? List<Int>
//        assertEquals(11, ls?.get(0))
//        assertEquals(22, ls?.get(1))
//        assertEquals(33, ls?.get(2))

        assertContentEquals(listOf(11, 22, 33), A::ls.decodeValue(listOf("11", "22", "33")) as? List<Int>)
        assertContentEquals(listOf(1, 2, 3), A::ls.decodeValue("1,2,3") as? List<Int>)
    }
}
package com.mapk.kmapper

import com.mapk.annotations.KParameterFlatten
import java.time.LocalDateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("KParameterFlattenテスト")
class KParameterFlattenTest {
    data class InnerDst(val fooFoo: Int, val barBar: String)
    data class Dst(@KParameterFlatten val bazBaz: InnerDst, val quxQux: LocalDateTime)

    private val expected = Dst(InnerDst(1, "str"), LocalDateTime.MIN)

    data class Src(val bazBazFooFoo: Int, val bazBazBarBar: String, val quxQux: LocalDateTime, val quuxQuux: Boolean)
    private val src = Src(1, "str", LocalDateTime.MIN, false)

    @Test
    @DisplayName("BoundKMapper")
    fun boundKMapperTest() {
        val result = BoundKMapper(::Dst, Src::class).map(src)
        assertEquals(expected, result)
    }

    @Test
    @DisplayName("KMapper")
    fun kMapperTest() {
        val result = KMapper(::Dst).map(src)
        assertEquals(expected, result)
    }

    @Test
    @DisplayName("PlainKMapper")
    fun plainKMapperTest() {
        val result = PlainKMapper(::Dst).map(src)
        assertEquals(expected, result)
    }
}

package com.mapk.kmapper

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("再帰的マッピングのテスト")
class RecursiveMappingTest {
    private data class InnerSrc(val hogeHoge: Int, val fugaFuga: Short, val piyoPiyo: String)
    private data class InnerDst(val hogeHoge: Int, val piyoPiyo: String)

    private data class Src(val fooFoo: InnerSrc, val barBar: Boolean, val bazBaz: Int)
    private data class Dst(val fooFoo: InnerDst, val bazBaz: Int)

    companion object {
        private val src: Src = Src(InnerSrc(1, 2, "three"), true, 4)
        private val expected: Dst = Dst(InnerDst(1, "three"), 4)
    }

    @Nested
    @DisplayName("BoundKMapper")
    inner class BoundKMapperTest {
        @Test
        fun test() {
            val actual = BoundKMapper(::Dst, Src::class).map(src)
            assertEquals(expected, actual)
        }
    }
}

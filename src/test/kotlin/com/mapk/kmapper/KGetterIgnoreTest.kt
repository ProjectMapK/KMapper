package com.mapk.kmapper

import com.mapk.annotations.KGetterIgnore
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class KGetterIgnoreTest {
    data class Src1(val arg1: Int, val arg2: String, @get:KGetterIgnore val arg3: Short)
    data class Src2(@get:KGetterIgnore val arg2: String, val arg3: Int, val arg4: String)

    data class Dst(val arg1: Int, val arg2: String, val arg3: Int, val arg4: String)

    @Nested
    @DisplayName("KMapper")
    inner class KMapperTest {
        @Test
        @DisplayName("フィールドを無視するテスト")
        fun test() {
            val src1 = Src1(1, "2-1", 31)
            val src2 = Src2("2-2", 32, "4")

            val mapper = KMapper(::Dst)

            val dst1 = mapper.map(src1, src2)
            val dst2 = mapper.map(src2, src1)

            assertTrue(dst1 == dst2)
            assertEquals(Dst(1, "2-1", 32, "4"), dst1)
        }
    }

    @Nested
    @DisplayName("PlainKMapper")
    inner class PlainKMapperTest {
        @Test
        @DisplayName("フィールドを無視するテスト")
        fun test() {
            val src1 = Src1(1, "2-1", 31)
            val src2 = Src2("2-2", 32, "4")

            val mapper = PlainKMapper(::Dst)

            val dst1 = mapper.map(src1, src2)
            val dst2 = mapper.map(src2, src1)

            assertTrue(dst1 == dst2)
            assertEquals(Dst(1, "2-1", 32, "4"), dst1)
        }
    }

    data class BoundSrc(val arg1: Int, val arg2: Short, @get:KGetterIgnore val arg3: String)
    data class BoundDst(val arg1: Int, val arg2: Short, val arg3: String = "default")

    @Nested
    @DisplayName("BoundKMapper")
    inner class BoundKMapperTest {
        @Test
        @DisplayName("フィールドを無視するテスト")
        fun test() {
            val mapper = BoundKMapper(::BoundDst, BoundSrc::class)

            val result = mapper.map(BoundSrc(1, 2, "arg3"))

            assertEquals(BoundDst(1, 2, "default"), result)
        }
    }
}

package com.mapk.kmapper

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

private data class StringMappingDst(val value: String)
private data class BoundMappingSrc(val value: Int)

@DisplayName("文字列に対してtoStringしたものを渡すテスト")
class StringMappingTest {
    @Nested
    @DisplayName("KMapper")
    inner class KMapperTest {
        @Test
        fun test() {
            val result: StringMappingDst = PlainKMapper(StringMappingDst::class).map("value" to 1)
            assertEquals("1", result.value)
        }
    }

    @Nested
    @DisplayName("BoundKMapper")
    inner class BoundKMapperTest {
        @Test
        fun test() {
            val result = BoundKMapper(::StringMappingDst, BoundMappingSrc::class).map(BoundMappingSrc(100))
            assertEquals("100", result.value)
        }
    }
}

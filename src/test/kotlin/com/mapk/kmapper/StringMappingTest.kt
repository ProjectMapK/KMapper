package com.mapk.kmapper

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

private data class StringMappingDst(val value: String)

@DisplayName("文字列に対してtoStringしたものを渡すテスト")
class StringMappingTest {
    @Nested
    @DisplayName("KMapperの場合")
    inner class KMapperTest {
        @Test
        fun test() {
            val result: StringMappingDst = KMapper(StringMappingDst::class).map("value" to 1)
            assertEquals("1", result.value)
        }
    }
}

package com.mapk.kmapper

import com.google.common.base.CaseFormat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

private data class CamelCaseDst(val camelCase: String)
private data class BoundSrc(val camel_case: String)

@DisplayName("パラメータ名変換のテスト")
class ParameterNameConverterTest {
    @Nested
    @DisplayName("KMapper")
    inner class KMapperTest {
        @Test
        @DisplayName("スネークケースsrc -> キャメルケースdst")
        fun test() {
            val expected = "snakeCase"
            val src = mapOf("camel_case" to expected)

            val mapper = PlainKMapper(CamelCaseDst::class) {
                CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, it)
            }
            val result = mapper.map(src)

            assertEquals(expected, result.camelCase)
        }
    }

    @Nested
    @DisplayName("BoundKMapper")
    inner class BoundKMapperTest {
        @Test
        @DisplayName("スネークケースsrc -> キャメルケースdst")
        fun test() {

            val mapper = BoundKMapper(::CamelCaseDst, BoundSrc::class) {
                CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, it)
            }
            val result = mapper.map(BoundSrc("snakeCase"))

            assertEquals(CamelCaseDst("snakeCase"), result)
        }
    }
}

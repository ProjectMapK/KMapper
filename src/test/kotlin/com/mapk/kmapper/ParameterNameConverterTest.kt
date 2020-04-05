package com.mapk.kmapper

import com.google.common.base.CaseFormat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

private class CamelCaseDst(val camelCase: String)

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

            val mapper = KMapper(CamelCaseDst::class) {
                CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, it)
            }
            val result = mapper.map(src)

            assertEquals(expected, result.camelCase)
        }
    }
}

@file:Suppress("unused")

package com.mapk.kmapper

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

enum class JvmLanguage {
    Java, Scala, Groovy, Kotlin
}

private class EnumMappingDst(val language: JvmLanguage?)

@DisplayName("文字列 -> Enumのマッピングテスト")
class EnumMappingTest {
    @Nested
    @DisplayName("PlainKMapper")
    inner class PlainKMapperTest {
        private val mapper = PlainKMapper(EnumMappingDst::class)

        @ParameterizedTest(name = "Non-Null要求")
        @EnumSource(value = JvmLanguage::class)
        fun test(language: JvmLanguage) {
            val result = mapper.map("language" to language.name)

            assertEquals(language, result.language)
        }
    }

    data class BoundSrc(val language: String)

    @Nested
    @DisplayName("BoundKMapper")
    inner class BoundKMapperTest {
        private val mapper = BoundKMapper(::EnumMappingDst, BoundSrc::class)

        @ParameterizedTest(name = "Non-Null要求")
        @EnumSource(value = JvmLanguage::class)
        fun test(language: JvmLanguage) {
            val result = mapper.map(BoundSrc(language.name))

            assertEquals(language, result.language)
        }
    }
}

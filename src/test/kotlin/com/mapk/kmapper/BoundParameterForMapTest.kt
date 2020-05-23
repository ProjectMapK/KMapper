package com.mapk.kmapper

import com.mapk.kmapper.testcommons.JvmLanguage
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaGetter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("BoundKMapperのパラメータテスト")
class BoundParameterForMapTest {
    data class IntSrc(val int: Int?)
    data class StringSrc(val str: String?)

    @Nested
    @DisplayName("Plainのテスト")
    inner class PlainTest {
        private val parameter =
            BoundParameterForMap.Plain<StringSrc>("", StringSrc::class.memberProperties.single().javaGetter!!)

        @Test
        @DisplayName("not null")
        fun isNotNull() {
            val result = parameter.map(StringSrc("sss"))
            assertEquals("sss", result)
        }

        @Test
        @DisplayName("null")
        fun isNull() {
            assertNull(parameter.map(StringSrc(null)))
        }
    }

    @Nested
    @DisplayName("ToEnumのテスト")
    inner class ToEnumTest {
        private val parameter = BoundParameterForMap.ToEnum<StringSrc>(
            "", StringSrc::class.memberProperties.single().javaGetter!!, JvmLanguage::class.java
        )

        @Test
        @DisplayName("not null")
        fun isNotNull() {
            val result = parameter.map(StringSrc("Java"))
            assertEquals(JvmLanguage.Java, result)
        }

        @Test
        @DisplayName("null")
        fun isNull() {
            assertNull(parameter.map(StringSrc(null)))
        }
    }

    @Nested
    @DisplayName("ToStringのテスト")
    inner class ToStringTest {
        private val parameter =
            BoundParameterForMap.ToString<IntSrc>("", IntSrc::class.memberProperties.single().javaGetter!!)

        @Test
        @DisplayName("not null")
        fun isNotNull() {
            val result = parameter.map(IntSrc(1))
            assertEquals("1", result)
        }

        @Test
        @DisplayName("null")
        fun isNull() {
            assertNull(parameter.map(IntSrc(null)))
        }
    }
}

package com.mapk.kmapper

import com.mapk.kmapper.testcommons.JvmLanguage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaGetter

@DisplayName("BoundKMapperのパラメータテスト")
class BoundParameterForMapTest {
    data class IntSrc(val int: Int?)
    data class StringSrc(val str: String?)
    data class InnerSrc(val int: Int?, val str: String?)
    data class ObjectSrc(val obj: Any?)

    data class ObjectDst(val int: Int?, val str: String?)

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
    @DisplayName("UseConverterのテスト")
    inner class UseConverterTest {
        // アクセシビリティの問題で公開状態に設定
        @Suppress("MemberVisibilityCanBePrivate")
        fun makeTwiceOrNull(int: Int?) = int?.let { it * 2 }

        private val parameter = BoundParameterForMap.UseConverter<IntSrc>(
            "", IntSrc::class.memberProperties.single().javaGetter!!, this::makeTwiceOrNull
        )

        @Test
        @DisplayName("not null")
        fun isNotNull() {
            val result = parameter.map(IntSrc(1))
            assertEquals(2, result)
        }

        @Test
        @DisplayName("null")
        fun isNull() {
            assertNull(parameter.map(IntSrc(null)))
        }
    }

    @Nested
    @DisplayName("UseKMapperのテスト")
    inner class UseKMapperTest {
        private val parameter = BoundParameterForMap.UseKMapper<ObjectSrc>(
            "", ObjectSrc::class.memberProperties.single().javaGetter!!, KMapper(::ObjectDst)
        )

        @Test
        @DisplayName("not null")
        fun isNotNull() {
            val result = parameter.map(ObjectSrc(mapOf("int" to 0, "str" to null)))
            assertEquals(ObjectDst(0, null), result)
        }

        @Test
        @DisplayName("null")
        fun isNull() {
            assertNull(parameter.map(ObjectSrc(null)))
        }
    }

    @Nested
    @DisplayName("UseBoundKMapperのテスト")
    inner class UseBoundKMapperTest {
        private val parameter = BoundParameterForMap.UseBoundKMapper<ObjectSrc, InnerSrc>(
            "", ObjectSrc::class.memberProperties.single().javaGetter!!, BoundKMapper(::ObjectDst, InnerSrc::class)
        )

        @Test
        @DisplayName("not null")
        fun isNotNull() {
            val result = parameter.map(ObjectSrc(InnerSrc(null, "str")))
            assertEquals(ObjectDst(null, "str"), result)
        }

        @Test
        @DisplayName("null")
        fun isNull() {
            assertNull(parameter.map(ObjectSrc(null)))
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

package com.mapk.kmapper

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

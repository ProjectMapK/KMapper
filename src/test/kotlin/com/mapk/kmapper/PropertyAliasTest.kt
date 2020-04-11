package com.mapk.kmapper

import com.mapk.annotations.KGetterAlias
import com.mapk.annotations.KParameterAlias
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

private data class AliasedDst(
    val arg1: Double,
    @param:KParameterAlias("arg3") val arg2: Int
)

private data class AliasedSrc(
    @get:KGetterAlias("arg1")
    val arg2: Double,
    val arg3: Int
)

@DisplayName("エイリアスを貼った場合のテスト")
class PropertyAliasTest {
    @Nested
    @DisplayName("KMapper")
    inner class KMapperTest {
        @Test
        @DisplayName("パラメータにエイリアスを貼った場合")
        fun paramAliasTest() {
            val src = mapOf(
                "arg1" to 1.0,
                "arg2" to "2",
                "arg3" to 3
            )

            val result = PlainKMapper(::AliasedDst).map(src)

            assertEquals(1.0, result.arg1)
            assertEquals(3, result.arg2)
        }

        @Test
        @DisplayName("ゲッターにエイリアスを貼った場合")
        fun getAliasTest() {
            val src = AliasedSrc(1.0, 2)
            val result = PlainKMapper(::AliasedDst).map(src)

            assertEquals(1.0, result.arg1)
            assertEquals(2, result.arg2)
        }
    }

    @Nested
    @DisplayName("BoundKMapper")
    inner class BoundKMapperTest {
        @Test
        @DisplayName("パラメータとゲッターそれぞれエイリアス有りでのマッピング")
        fun test() {
            val mapper = BoundKMapper(::AliasedDst, AliasedSrc::class)

            val result = mapper.map(AliasedSrc(2.2, 100))

            assertEquals(AliasedDst(2.2, 100), result)
        }
    }
}

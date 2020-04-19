package com.mapk.kmapper

import com.google.common.base.CaseFormat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("再帰的マッピングのテスト")
class RecursiveMappingTest {
    private data class InnerSrc(val hogeHoge: Int, val fugaFuga: Short, val piyoPiyo: String)
    private data class InnerSnakeSrc(val hoge_hoge: Int, val fuga_fuga: Short, val piyo_piyo: String)
    private data class InnerDst(val hogeHoge: Int, val piyoPiyo: String)

    private data class Src(val fooFoo: InnerSrc, val barBar: Boolean, val bazBaz: Int)
    private data class SnakeSrc(val foo_foo: InnerSnakeSrc, val bar_bar: Boolean, val baz_baz: Int)
    private data class MapSrc(val fooFoo: Map<String, Any>, val barBar: Boolean, val bazBaz: Int)
    private data class Dst(val fooFoo: InnerDst, val bazBaz: Int)

    companion object {
        private val src: Src = Src(InnerSrc(1, 2, "three"), true, 4)
        private val snakeSrc: SnakeSrc = SnakeSrc(InnerSnakeSrc(1, 2, "three"), true, 4)
        private val mapSrc: MapSrc = MapSrc(mapOf("hogeHoge" to 1, "piyoPiyo" to "three"), true, 4)
        private val expected: Dst = Dst(InnerDst(1, "three"), 4)
    }

    @Nested
    @DisplayName("KMapper")
    inner class KMapperTest {
        @Test
        @DisplayName("シンプルなマッピング")
        fun test() {
            val actual = KMapper(::Dst).map(src)
            assertEquals(expected, actual)
        }

        @Test
        @DisplayName("スネークケースsrc -> キャメルケースdst")
        fun snakeToCamel() {
            val actual = KMapper(::Dst) {
                CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, it)
            }.map(snakeSrc)
            assertEquals(expected, actual)
        }

        @Test
        @DisplayName("内部フィールドがMapの場合")
        fun includesMap() {
            val actual = KMapper(::Dst).map(mapSrc)
            assertEquals(expected, actual)
        }
    }

    @Nested
    @DisplayName("BoundKMapper")
    inner class BoundKMapperTest {
        @Test
        @DisplayName("シンプルなマッピング")
        fun test() {
            val actual = BoundKMapper(::Dst, Src::class).map(src)
            assertEquals(expected, actual)
        }

        @Test
        @DisplayName("スネークケースsrc -> キャメルケースdst")
        fun snakeToCamel() {
            val actual = BoundKMapper(::Dst, SnakeSrc::class) {
                CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, it)
            }.map(snakeSrc)
            assertEquals(expected, actual)
        }

        @Test
        @DisplayName("内部フィールドがMapの場合")
        fun includesMap() {
            val actual = BoundKMapper(::Dst, MapSrc::class).map(mapSrc)
            assertEquals(expected, actual)
        }
    }
}

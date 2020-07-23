package com.mapk.kmapper

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

@Suppress("UNUSED_VARIABLE")
@DisplayName("BoundKMapperの初期化時にエラーを吐かせるテスト")
internal class BoundKMapperInitTest {
    data class Dst1(val foo: Int, val bar: Short, val baz: Long)
    data class Dst2(val foo: Int, val bar: Short, val baz: Long = 0)

    data class Src(val foo: Int, val bar: Short)

    @Test
    @DisplayName("引数が足りない場合のエラーテスト")
    fun isError() {
        assertThrows<IllegalArgumentException> {
            val mapper: BoundKMapper<Src, Dst1> = BoundKMapper()
        }
    }

    @Test
    @DisplayName("足りないのがオプショナルな引数の場合エラーにならないテスト")
    fun isCollect() {
        assertDoesNotThrow { val mapper: BoundKMapper<Src, Dst2> = BoundKMapper(::Dst2) }
    }
}

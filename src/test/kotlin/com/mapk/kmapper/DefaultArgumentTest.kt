package com.mapk.kmapper

import com.mapk.annotations.KUseDefaultArgument
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("デフォルト引数を指定するテスト")
class DefaultArgumentTest {
    data class Dst(val fooArgument: Int, @param:KUseDefaultArgument val barArgument: String = "default")
    data class Src(val fooArgument: Int, val barArgument: String)

    @Nested
    @DisplayName("KMapper")
    inner class KMapperTest {
        @Test
        fun test() {
            val src = Src(1, "src")

            val result = KMapper(::Dst).map(src)
            assertEquals(Dst(1, "default"), result)
        }
    }
}

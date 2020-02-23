package mapk.core

import com.wrongwrong.mapk.annotations.KPropertyAlias
import com.wrongwrong.mapk.core.KMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

private data class AliasedDst(
    val arg1: Double,
    @param:KPropertyAlias("arg3") val arg2: Int
)

private data class AliasedSrc(
    @get:KPropertyAlias("arg1") val arg2: Double,
    val arg3: Int
)

@DisplayName("エイリアスを貼った場合のテスト")
class ParamAliasTest {
    @Test
    @DisplayName("パラメータにエイリアスを貼った場合")
    fun paramAliasTest() {
        val src = mapOf(
            "arg1" to 1.0,
            "arg2" to "2",
            "arg3" to 3
        )

        val result = KMapper(::AliasedDst).map(src)

        assertEquals(1.0, result.arg1)
        assertEquals(3, result.arg2)
    }

    @Test
    @DisplayName("ゲッターにエイリアスを貼った場合")
    fun getAliasTest() {
        val src = AliasedSrc(1.0, 2)
        val result = KMapper(::AliasedDst).map(src)

        assertEquals(1.0, result.arg1)
        assertEquals(2, result.arg2)
    }
}

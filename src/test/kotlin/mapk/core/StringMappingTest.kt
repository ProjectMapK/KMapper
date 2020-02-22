package mapk.core

import com.wrongwrong.mapk.core.KMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

private data class StringMappingDst(val value: String)

@DisplayName("文字列に対してtoStringしたものを渡すテスト")
class StringMappingTest {
    @Test
    fun test() {
        val result: StringMappingDst = KMapper(StringMappingDst::class).map("value" to 1)
        assertEquals("1", result.value)
    }
}

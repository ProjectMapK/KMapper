package mapk.core

import com.google.common.base.CaseFormat
import com.wrongwrong.mapk.core.KMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

private class CamelCaseDst(val camelCase: String)

@DisplayName("プロパティ名変換のテスト")
class PropertyNameConverterTest {
    @Test
    @DisplayName("スネークケースsrc -> キャメルケースdst")
    fun test() {
        val expected = "snakeCase"
        val src = mapOf("camel_case" to expected)

        val mapper = KMapper(CamelCaseDst::class) { CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, it) }
        val result = mapper.map(src)

        assertEquals(expected, result.camelCase)
    }
}

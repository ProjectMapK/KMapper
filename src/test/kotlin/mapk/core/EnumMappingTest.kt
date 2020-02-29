@file:Suppress("unused")

package mapk.core

import com.mapk.core.KMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

enum class JvmLanguage {
    Java, Scala, Groovy, Kotlin
}

private class EnumMappingDst(val language: JvmLanguage?)

@DisplayName("文字列 -> Enumのマッピングテスト")
class EnumMappingTest {
    private val mapper = KMapper(EnumMappingDst::class)

    @ParameterizedTest(name = "Non-Null要求")
    @EnumSource(value = JvmLanguage::class)
    fun test(language: JvmLanguage) {
        val result = mapper.map("language" to language.name)

        assertEquals(language, result.language)
    }
}

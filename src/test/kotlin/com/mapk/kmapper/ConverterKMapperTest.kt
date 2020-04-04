@file:Suppress("unused")

package com.mapk.kmapper

import com.mapk.annotations.KConverter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

private data class ConstructorConverterDst(val argument: ConstructorConverter)
private data class ConstructorConverter @KConverter constructor(val arg: Number)

private data class CompanionConverterDst(val argument: CompanionConverter)
// NOTE: privateクラスのcompanion objectにアクセスする方法を見つけられなかった
class CompanionConverter private constructor(val arg: String) {
    private companion object {
        @KConverter
        private fun converter(arg: String): CompanionConverter {
            return CompanionConverter(arg)
        }
    }
}

private data class StaticMethodConverterDst(val argument: StaticMethodConverter)

@DisplayName("コンバータ有りでのマッピングテスト")
class ConverterKMapperTest {
    @Nested
    @DisplayName("KMapper")
    inner class KMapperTest {
        @Test
        @DisplayName("コンストラクターでのコンバートテスト")
        fun constructorConverterTest() {
            val mapper = KMapper(ConstructorConverterDst::class)
            val result = mapper.map(mapOf("argument" to 1))

            assertEquals(ConstructorConverter(1), result.argument)
        }

        @Test
        @DisplayName("コンパニオンオブジェクトに定義したコンバータでのコンバートテスト")
        fun companionConverterTest() {
            val mapper = KMapper(CompanionConverterDst::class)
            val result = mapper.map(mapOf("argument" to "arg"))

            assertEquals("arg", result.argument.arg)
        }

        @Test
        @DisplayName("スタティックメソッドに定義したコンバータでのコンバートテスト")
        fun staticMethodConverterTest() {
            val mapper = KMapper(StaticMethodConverterDst::class)
            val result = mapper.map(mapOf("argument" to "1,2,3"))

            assertTrue(intArrayOf(1, 2, 3) contentEquals result.argument.arg)
        }
    }
}

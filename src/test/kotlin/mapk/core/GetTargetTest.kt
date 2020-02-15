@file:Suppress("unused")

package mapk.core

import com.wrongwrong.mapk.annotations.KConstructor
import com.wrongwrong.mapk.core.getTarget
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.reflect.full.primaryConstructor

private class SecondaryConstructorDst(val argument: Int) {
    @KConstructor constructor(argument: Number) : this(argument.toInt())
}
class CompanionFactoryDst(val argument: IntArray) {
    companion object {
        @KConstructor
        fun factory(csv: String): CompanionFactoryDst {
            return csv.split(",").map { it.toInt() }.toIntArray().let { CompanionFactoryDst(it) }
        }
    }
}
private class ConstructorDst(val argument: String)
class MultipleConstructorDst @KConstructor constructor(val argument: Int) {
    @KConstructor constructor(argument: String): this(argument.toInt())
}

@DisplayName("クラスからのコンストラクタ抽出関連テスト")
class GetTargetTest {
    @Test
    @DisplayName("セカンダリコンストラクタからの取得テスト")
    fun testGetFromSecondaryConstructor() {
        val function = getTarget(SecondaryConstructorDst::class)
        assertTrue(function.annotations.any { it is KConstructor })
    }

    @Test
    @DisplayName("ファクトリーメソッドからの取得テスト")
    fun testGetFromFactoryMethod() {
        val function = getTarget(CompanionFactoryDst::class)
        assertTrue(function.annotations.any { it is KConstructor })
    }

    @Test
    @DisplayName("無指定でプライマリコンストラクタからの取得テスト")
    fun testGetFromPrimaryConstructor() {
        val function = getTarget(ConstructorDst::class)
        assertEquals(ConstructorDst::class.primaryConstructor, function)
    }

    @Test
    @DisplayName("対象を複数指定した場合のテスト")
    fun testMultipleDeclareConstructor() {
        assertThrows<IllegalArgumentException> { getTarget(MultipleConstructorDst::class) }
    }
}

@file:Suppress("unused")

package mapk.core

import com.wrongwrong.mapk.annotations.KConstructor
import com.wrongwrong.mapk.core.KFunctionForCall
import com.wrongwrong.mapk.core.getTarget
import kotlin.reflect.KFunction
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

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
    @KConstructor constructor(argument: String) : this(argument.toInt())
}

@Suppress("UNCHECKED_CAST")
@DisplayName("クラスからのコンストラクタ抽出関連テスト")
class GetTargetTest {
    private fun <T : Any> KFunctionForCall<T>.getTargetFunction(): KFunction<T> {
        return this::class.memberProperties.first { it.name == "function" }.getter.let {
            it.isAccessible = true
            it.call(this) as KFunction<T>
        }
    }

    @Test
    @DisplayName("セカンダリコンストラクタからの取得テスト")
    fun testGetFromSecondaryConstructor() {
        val function = getTarget(SecondaryConstructorDst::class).getTargetFunction()
        assertTrue(function.annotations.any { it is KConstructor })
    }

    @Test
    @DisplayName("ファクトリーメソッドからの取得テスト")
    fun testGetFromFactoryMethod() {
        val function = getTarget(SecondaryConstructorDst::class).getTargetFunction()
        assertTrue(function.annotations.any { it is KConstructor })
    }

    @Test
    @DisplayName("無指定でプライマリコンストラクタからの取得テスト")
    fun testGetFromPrimaryConstructor() {
        val function = getTarget(ConstructorDst::class).getTargetFunction()
        assertEquals(ConstructorDst::class.primaryConstructor, function)
    }

    @Test
    @DisplayName("対象を複数指定した場合のテスト")
    fun testMultipleDeclareConstructor() {
        assertThrows<IllegalArgumentException> { getTarget(MultipleConstructorDst::class) }
    }
}

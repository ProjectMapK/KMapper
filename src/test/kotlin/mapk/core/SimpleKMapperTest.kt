@file:Suppress("unused")

package mapk.core

import com.wrongwrong.mapk.core.KMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.math.BigInteger
import kotlin.reflect.full.primaryConstructor

private data class SimpleDst(
    val arg1: Int,
    val arg2: String?,
    val arg3: Number
) {
    companion object {
        fun factory(arg1: Int, arg2: String?, arg3: Number): SimpleDst {
            return SimpleDst(arg1, arg2, arg3)
        }
    }
}

private data class Src1(
    val arg2: String?
) {
    val arg1: Int = arg2?.length ?: 0
    val arg3: Number
        get() = arg1.toByte()
    val arg4 = null
}

private data class Src2(val arg2: String?)

@DisplayName("単純なマッピングのテスト")
class SimpleKMapperTest {
    private fun instanceFunction(arg1: Int, arg2: String?, arg3: Number): SimpleDst {
        return SimpleDst(arg1, arg2, arg3)
    }

    private val mappers: Set<KMapper<SimpleDst>> = setOf(
        KMapper(SimpleDst::class),
        KMapper(SimpleDst::class.primaryConstructor!!),
        KMapper((SimpleDst)::factory),
        KMapper(this::instanceFunction)
    )

    @Nested
    @DisplayName("Map<String, Any?>からマップ")
    inner class FromMap {
        @Test
        @DisplayName("Nullを含まない場合")
        fun testWithoutNull() {
            val srcMap: Map<String, Any?> = mapOf(
                "arg1" to 2,
                "arg2" to "value",
                "arg3" to 1.0
            )

            val dsts = mappers.map { it.map(srcMap) }

            assertEquals(1, dsts.distinct().size)
            dsts.first().let {
                assertEquals(2, it.arg1)
                assertEquals("value", it.arg2)
                assertEquals(1.0, it.arg3)
            }
        }

        @Test
        @DisplayName("Nullを含む場合")
        fun testContainsNull() {
            val srcMap: Map<String, Any?> = mapOf(
                "arg1" to 1,
                "arg2" to null,
                "arg3" to 2.0f
            )

            val dsts = mappers.map { it.map(srcMap) }

            assertEquals(1, dsts.distinct().size)
            dsts.first().let {
                assertEquals(1, it.arg1)
                assertEquals(null, it.arg2)
                assertEquals(2.0f, it.arg3)
            }
        }
    }

    @Nested
    @DisplayName("インスタンスからマップ")
    inner class FromInstance {
        @Test
        @DisplayName("Nullを含まない場合")
        fun testWithoutNull() {
            val stringValue = "value"

            val src = Src1(stringValue)

            val dsts = mappers.map { it.map(src) }

            assertEquals(1, dsts.distinct().size)
            dsts.first().let {
                assertEquals(stringValue.length, it.arg1)
                assertEquals(stringValue, it.arg2)
                assertEquals(stringValue.length.toByte(), it.arg3)
            }
        }

        @Test
        @DisplayName("Nullを含む場合")
        fun testContainsNull() {
            val src = Src1(null)

            val dsts = mappers.map { it.map(src) }

            assertEquals(1, dsts.distinct().size)
            dsts.first().let {
                assertEquals(0, it.arg1)
                assertEquals(null, it.arg2)
                assertEquals(0.toByte(), it.arg3)
            }
        }
    }

    @Nested
    @DisplayName("複数ソースからのマップ")
    inner class FromMultipleSrc {
        @Test
        @DisplayName("Nullを含まない場合")
        fun testWithoutNull() {
            val src1 = "arg1" to 1
            val src2 = Src2("value")
            val src3 = mapOf("arg3" to 5.5)

            val dsts = mappers.map { it.map(src1, src2, src3) }

            assertEquals(1, dsts.distinct().size)
            dsts.first().let {
                assertEquals(1, it.arg1)
                assertEquals("value", it.arg2)
                assertEquals(5.5, it.arg3)
            }
        }

        @Test
        @DisplayName("Nullを含む場合")
        fun testContainsNull() {
            val src1 = "arg1" to 7
            val src2 = Src2(null)
            val src3 = mapOf("arg3" to BigInteger.TWO)

            val dsts = mappers.map { it.map(src1, src2, src3) }

            assertEquals(1, dsts.distinct().size)
            dsts.first().let {
                assertEquals(7, it.arg1)
                assertEquals(null, it.arg2)
                assertEquals(BigInteger.TWO, it.arg3)
            }
        }
    }
}

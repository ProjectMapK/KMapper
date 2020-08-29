@file:Suppress("unused")

package com.mapk.kmapper

import com.mapk.annotations.KConstructor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.math.BigInteger
import java.util.stream.Stream
import kotlin.reflect.full.isSubclassOf

open class SimpleDst(
    val arg1: Int,
    val arg2: String?,
    val arg3: Number
) {
    companion object {
        fun factory(arg1: Int, arg2: String?, arg3: Number): SimpleDst {
            return SimpleDst(arg1, arg2, arg3)
        }
    }

    override fun equals(other: Any?): Boolean {
        return other?.takeIf { other::class.isSubclassOf(SimpleDst::class) }?.let {
            it as SimpleDst

            return this.arg1 == it.arg1 && this.arg2 == it.arg2 && this.arg3 == it.arg3
        } ?: false
    }

    override fun hashCode(): Int {
        var result = arg1
        result = 31 * result + (arg2?.hashCode() ?: 0)
        result = 31 * result + arg3.hashCode()
        return result
    }
}

class SimpleDstExt(
    arg1: Int,
    arg2: String?,
    arg3: Number
) : SimpleDst(arg1, arg2, arg3) {
    companion object {
        @KConstructor
        fun factory(arg1: Int, arg2: String?, arg3: Number): SimpleDstExt {
            return SimpleDstExt(arg1, arg2, arg3)
        }
    }
}

data class Src1(
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

    @Nested
    @DisplayName("KMapper")
    inner class KMapperTest {
        private val mappers: Set<KMapper<out SimpleDst>> = setOf(
            KMapper(SimpleDst::class),
            KMapper(::SimpleDst),
            KMapper((SimpleDst)::factory),
            KMapper(::instanceFunction),
            KMapper(SimpleDstExt::class)
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
    }

    @Nested
    @DisplayName("PlainKMapper")
    inner class PlainKMapperTest {
        private val mappers: Set<PlainKMapper<out SimpleDst>> = setOf(
            PlainKMapper(SimpleDst::class),
            PlainKMapper(::SimpleDst),
            PlainKMapper((SimpleDst)::factory),
            PlainKMapper(::instanceFunction),
            PlainKMapper(SimpleDstExt::class)
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
                val two = BigInteger.valueOf(2L)

                val src1 = "arg1" to 7
                val src2 = Src2(null)
                val src3 = mapOf("arg3" to two)

                val dsts = mappers.map { it.map(src1, src2, src3) }

                assertEquals(1, dsts.distinct().size)
                dsts.first().let {
                    assertEquals(7, it.arg1)
                    assertEquals(null, it.arg2)
                    assertEquals(two, it.arg3)
                }
            }
        }
    }

    @Nested
    @DisplayName("BoundKMapper")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class BoundKMapperTest {
        @Nested
        @DisplayName("インスタンスからマップ")
        @TestInstance(TestInstance.Lifecycle.PER_CLASS)
        inner class FromInstance {
            fun boundKMapperProvider(): Stream<Arguments> = Stream.of(
                arguments("from method reference", BoundKMapper(::SimpleDst, Src1::class)),
                arguments("from class", BoundKMapper(SimpleDst::class, Src1::class))
            )

            @ParameterizedTest(name = "Nullを含まない場合")
            @MethodSource("boundKMapperProvider")
            fun testWithoutNull(
                @Suppress("UNUSED_PARAMETER") name: String,
                mapper: BoundKMapper<Src1, SimpleDst>
            ) {
                val stringValue = "value"

                val src = Src1(stringValue)

                val dst = mapper.map(src)

                assertEquals(stringValue.length, dst.arg1)
                assertEquals(stringValue, dst.arg2)
                assertEquals(stringValue.length.toByte(), dst.arg3)
            }

            @ParameterizedTest(name = "Nullを含む場合")
            @MethodSource("boundKMapperProvider")
            fun testContainsNull(
                @Suppress("UNUSED_PARAMETER") name: String,
                mapper: BoundKMapper<Src1, SimpleDst>
            ) {
                val src = Src1(null)

                val dst = mapper.map(src)

                assertEquals(0, dst.arg1)
                assertEquals(null, dst.arg2)
                assertEquals(0.toByte(), dst.arg3)
            }
        }
    }
}

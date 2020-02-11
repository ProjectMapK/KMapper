package mapk.core

import com.wrongwrong.mapk.core.KMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.reflect.full.primaryConstructor

data class Dst(
    val arg1: Int,
    val arg2: String?,
    val arg3: Number
) {
    companion object {
        fun factory(arg1: Int, arg2: String?, arg3: Number): Dst {
            return Dst(arg1, arg2, arg3)
        }
    }
}

@DisplayName("単純なマッピングのテスト")
class SimpleKMapperTest {
    val mappers: Set<KMapper<Dst>> =
        setOf(KMapper(Dst::class), KMapper(Dst::class.primaryConstructor!!), KMapper((Dst)::factory))

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

            assertEquals(dsts.distinct().size, 1)
            dsts.first().let {
                assertEquals(it.arg1, 2)
                assertEquals(it.arg2, "value")
                assertEquals(it.arg3, 1.0)
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

            assertEquals(dsts.distinct().size, 1)
            dsts.first().let {
                assertEquals(it.arg1, 1)
                assertEquals(it.arg2, null)
                assertEquals(it.arg3, 2.0f)
            }
        }
    }
}

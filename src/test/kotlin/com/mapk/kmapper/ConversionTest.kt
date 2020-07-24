package com.mapk.kmapper

import com.mapk.conversion.AbstractKConverter
import com.mapk.conversion.KConvertBy
import java.lang.IllegalArgumentException
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmName
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource

@DisplayName("KConvertアノテーションによる変換のテスト")
class ConversionTest {
    @Target(AnnotationTarget.VALUE_PARAMETER)
    @Retention(AnnotationRetention.RUNTIME)
    @MustBeDocumented
    @KConvertBy([FromString::class, FromNumber::class])
    annotation class ToNumber(val destination: KClass<out Number>)

    class FromString(annotation: ToNumber) : AbstractKConverter<ToNumber, String, Number>(annotation) {
        private val converter: (String) -> Number = when (annotation.destination) {
            Double::class -> String::toDouble
            Float::class -> String::toFloat
            Long::class -> String::toLong
            Int::class -> String::toInt
            Short::class -> String::toShort
            Byte::class -> String::toByte
            BigDecimal::class -> { { BigDecimal(it) } }
            BigInteger::class -> { { BigInteger(it) } }
            else -> throw IllegalArgumentException("${annotation.destination.jvmName} is not supported.")
        }

        override val srcClass = String::class
        override fun convert(source: String): Number? = source.let(converter)
    }

    class FromNumber(annotation: ToNumber) : AbstractKConverter<ToNumber, Number, Number>(annotation) {
        private val converter: (Number) -> Number = when (annotation.destination) {
            Double::class -> Number::toDouble
            Float::class -> Number::toFloat
            Long::class -> Number::toLong
            Int::class -> Number::toInt
            Short::class -> Number::toShort
            Byte::class -> Number::toByte
            BigDecimal::class -> { { BigDecimal.valueOf(it.toDouble()) } }
            BigInteger::class -> { { BigInteger.valueOf(it.toLong()) } }
            else -> throw IllegalArgumentException("${annotation.destination.jvmName} is not supported.")
        }

        override val srcClass = Number::class
        override fun convert(source: Number): Number? = source.let(converter)
    }

    data class Dst(@ToNumber(BigDecimal::class) val number: BigDecimal)
    data class NumberSrc(val number: Number)
    data class StringSrc(val number: String)

    enum class NumberSource(val values: Array<Number>) {
        Doubles(arrayOf(1.0, -2.0, 3.5)),
        Floats(arrayOf(4.1f, -5.09f, 6.00001f)),
        Longs(arrayOf(7090, 800, 911)),
        Ints(arrayOf(0, 123, 234)),
        Shorts(arrayOf(365, 416, 511)),
        Bytes(arrayOf(6, 7, 8))
    }

    @Nested
    @DisplayName("KMapper")
    inner class KMapperTest {
        @ParameterizedTest
        @EnumSource(NumberSource::class)
        @DisplayName("Numberソース")
        fun fromNumber(numbers: NumberSource) {
            numbers.values.forEach {
                val actual = KMapper(::Dst).map(NumberSrc(it))
                assertEquals(0, BigDecimal.valueOf(it.toDouble()).compareTo(actual.number))
            }
        }

        @ParameterizedTest
        @ValueSource(strings = ["100", "2.0", "-500"])
        @DisplayName("Stringソース")
        fun fromString(str: String) {
            val actual = KMapper(::Dst).map(StringSrc(str))
            assertEquals(0, BigDecimal(str).compareTo(actual.number))
        }
    }

    @Nested
    @DisplayName("PlainKMapper")
    inner class PlainKMapperTest {
        @ParameterizedTest
        @EnumSource(NumberSource::class)
        @DisplayName("Numberソース")
        fun fromNumber(numbers: NumberSource) {
            numbers.values.forEach {
                val actual = PlainKMapper(::Dst).map(NumberSrc(it))
                assertEquals(0, BigDecimal.valueOf(it.toDouble()).compareTo(actual.number))
            }
        }

        @ParameterizedTest
        @ValueSource(strings = ["100", "2.0", "-500"])
        @DisplayName("Stringソース")
        fun fromString(str: String) {
            val actual = PlainKMapper(::Dst).map(StringSrc(str))
            assertEquals(0, BigDecimal(str).compareTo(actual.number))
        }
    }

    @Nested
    @DisplayName("BoundKMapper")
    inner class BoundKMapperTest {
        @ParameterizedTest
        @EnumSource(NumberSource::class)
        @DisplayName("Numberソース")
        fun fromNumber(numbers: NumberSource) {
            numbers.values.forEach {
                val actual = BoundKMapper<NumberSrc, Dst>().map(NumberSrc(it))
                assertEquals(0, BigDecimal.valueOf(it.toDouble()).compareTo(actual.number))
            }
        }

        @ParameterizedTest
        @ValueSource(strings = ["100", "2.0", "-500"])
        @DisplayName("Stringソース")
        fun fromString(str: String) {
            val actual = BoundKMapper<StringSrc, Dst>().map(StringSrc(str))
            assertEquals(0, BigDecimal(str).compareTo(actual.number))
        }
    }
}

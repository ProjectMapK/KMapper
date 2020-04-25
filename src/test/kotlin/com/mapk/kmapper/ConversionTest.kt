package com.mapk.kmapper

import com.mapk.conversion.AbstractKConverter
import com.mapk.conversion.KConvertBy
import org.junit.jupiter.api.DisplayName
import java.lang.IllegalArgumentException
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmName

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
        override fun convert(source: String?): Number? = source?.let(converter)
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
        override fun convert(source: Number?): Number? = source?.let(converter)
    }
}

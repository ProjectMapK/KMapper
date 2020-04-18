package com.mapk.kmapper

import com.mapk.core.EnumMapper
import java.lang.IllegalArgumentException
import java.lang.reflect.Method
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.javaGetter

@Suppress("UNCHECKED_CAST")
internal sealed class BoundParameterForMap<S> {
    abstract val param: KParameter
    abstract val propertyGetter: Method

    abstract fun map(src: S): Any?

    class Plain<S : Any>(
        override val param: KParameter,
        override val propertyGetter: Method
    ) : BoundParameterForMap<S>() {
        override fun map(src: S): Any? = propertyGetter.invoke(src)
    }

    class UseConverter<S : Any>(
        override val param: KParameter,
        override val propertyGetter: Method,
        private val converter: KFunction<*>
    ) : BoundParameterForMap<S>() {
        override fun map(src: S): Any? = converter.call(propertyGetter.invoke(src))
    }

    class ToEnum<S : Any>(
        override val param: KParameter,
        override val propertyGetter: Method,
        private val paramClazz: Class<*>
    ) : BoundParameterForMap<S>() {
        override fun map(src: S): Any? = EnumMapper.getEnum(paramClazz, propertyGetter.invoke(src) as String)
    }

    class ToString<S : Any>(
        override val param: KParameter,
        override val propertyGetter: Method
    ) : BoundParameterForMap<S>() {
        override fun map(src: S): String? = propertyGetter.invoke(src).toString()
    }

    companion object {
        fun <S : Any> newInstance(param: KParameter, property: KProperty1<S, *>): BoundParameterForMap<S> {
            // ゲッターが無いならエラー
            val propertyGetter = property.javaGetter
                ?: throw IllegalArgumentException("${property.name} does not have getter.")
            propertyGetter.isAccessible = true

            val paramClazz = param.type.classifier as KClass<*>
            val propertyClazz = property.returnType.classifier as KClass<*>

            // コンバータが取れた場合
            paramClazz.getConverters()
                .filter { (key, _) -> propertyClazz.isSubclassOf(key) }
                .let {
                    if (1 < it.size) throw IllegalArgumentException("${param.name} has multiple converter. $it")

                    it.singleOrNull()?.second
                }?.let {
                    return UseConverter(param, propertyGetter, it)
                }

            if (paramClazz.isSubclassOf(propertyClazz)) {
                return Plain(param, propertyGetter)
            }

            val javaClazz = paramClazz.java

            return when {
                javaClazz.isEnum && propertyClazz == String::class -> ToEnum(param, propertyGetter, javaClazz)
                paramClazz == String::class -> ToString(param, propertyGetter)
                else -> throw IllegalArgumentException("Can not convert $propertyClazz to $paramClazz")
            }
        }
    }
}

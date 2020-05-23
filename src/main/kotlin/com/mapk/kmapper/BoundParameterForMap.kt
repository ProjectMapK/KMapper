package com.mapk.kmapper

import com.mapk.core.EnumMapper
import com.mapk.core.ValueParameter
import java.lang.IllegalArgumentException
import java.lang.reflect.Method
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty1
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.javaGetter

@Suppress("UNCHECKED_CAST")
internal sealed class BoundParameterForMap<S> {
    abstract val name: String
    protected abstract val propertyGetter: Method

    abstract fun map(src: S): Any?

    internal class Plain<S : Any>(
        override val name: String,
        override val propertyGetter: Method
    ) : BoundParameterForMap<S>() {
        override fun map(src: S): Any? = propertyGetter.invoke(src)
    }

    private class UseConverter<S : Any>(
        override val name: String,
        override val propertyGetter: Method,
        private val converter: KFunction<*>
    ) : BoundParameterForMap<S>() {
        override fun map(src: S): Any? = converter.call(propertyGetter.invoke(src))
    }

    private class UseKMapper<S : Any>(
        override val name: String,
        override val propertyGetter: Method,
        private val kMapper: KMapper<*>
    ) : BoundParameterForMap<S>() {
        // 1引数で呼び出すとMap/Pairが適切に処理されないため、2引数目にダミーを噛ませている
        override fun map(src: S): Any? = kMapper.map(propertyGetter.invoke(src), PARAMETER_DUMMY)
    }

    private class UseBoundKMapper<S : Any, T : Any>(
        override val name: String,
        override val propertyGetter: Method,
        private val boundKMapper: BoundKMapper<T, *>
    ) : BoundParameterForMap<S>() {
        override fun map(src: S): Any? = boundKMapper.map(propertyGetter.invoke(src) as T)
    }

    internal class ToEnum<S : Any>(
        override val name: String,
        override val propertyGetter: Method,
        private val paramClazz: Class<*>
    ) : BoundParameterForMap<S>() {
        override fun map(src: S): Any? = EnumMapper.getEnum(paramClazz, propertyGetter.invoke(src) as String?)
    }

    internal class ToString<S : Any>(
        override val name: String,
        override val propertyGetter: Method
    ) : BoundParameterForMap<S>() {
        override fun map(src: S): String? = propertyGetter.invoke(src)?.toString()
    }

    companion object {
        fun <S : Any> newInstance(
            param: ValueParameter<*>,
            property: KProperty1<S, *>,
            parameterNameConverter: (String) -> String
        ): BoundParameterForMap<S> {
            // ゲッターが無いならエラー
            val propertyGetter = property.javaGetter
                ?: throw IllegalArgumentException("${property.name} does not have getter.")
            propertyGetter.isAccessible = true

            val paramClazz = param.requiredClazz
            val propertyClazz = property.returnType.classifier as KClass<*>

            // コンバータが取れた場合
            (param.getConverters() + paramClazz.getConverters())
                .filter { (key, _) -> propertyClazz.isSubclassOf(key) }
                .let {
                    if (1 < it.size) throw IllegalArgumentException("${param.name} has multiple converter. $it")

                    it.singleOrNull()?.second
                }?.let {
                    return UseConverter(param.name, propertyGetter, it)
                }

            if (paramClazz.isSubclassOf(propertyClazz)) {
                return Plain(param.name, propertyGetter)
            }

            val javaClazz = paramClazz.java

            return when {
                javaClazz.isEnum && propertyClazz == String::class -> ToEnum(param.name, propertyGetter, javaClazz)
                paramClazz == String::class -> ToString(param.name, propertyGetter)
                // SrcがMapやPairならKMapperを使わないとマップできない
                propertyClazz.isSubclassOf(Map::class) || propertyClazz.isSubclassOf(Pair::class) -> UseKMapper(
                    param.name, propertyGetter, KMapper(paramClazz, parameterNameConverter)
                )
                // 何にも当てはまらなければBoundKMapperでマップを試みる
                else -> UseBoundKMapper(
                    param.name, propertyGetter, BoundKMapper(paramClazz, propertyClazz, parameterNameConverter)
                )
            }
        }
    }
}

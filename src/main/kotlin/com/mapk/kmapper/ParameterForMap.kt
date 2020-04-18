package com.mapk.kmapper

import com.mapk.core.EnumMapper
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.isSuperclassOf

internal class ParameterForMap<T : Any> private constructor(val param: KParameter, private val clazz: KClass<T>) {
    private val javaClazz: Class<T> by lazy {
        clazz.java
    }
    // リストの長さが小さいと期待されるためこの形で実装しているが、理想的にはmap的なものが使いたい
    private val converters: Set<Pair<KClass<*>, KFunction<T>>> = clazz.getConverters()

    private val convertCache: ConcurrentMap<KClass<*>, ParameterProcessor> = ConcurrentHashMap()

    fun <U : Any> mapObject(value: U): Any? {
        val valueClazz: KClass<*> = value::class

        // 取得方法のキャッシュが有ればそれを用いる
        convertCache[valueClazz]?.let { return it.process(value) }

        // パラメータに対してvalueが代入可能（同じもしくは親クラス）であればそのまま用いる
        if (clazz.isSuperclassOf(valueClazz)) {
            convertCache.putIfAbsent(valueClazz, ParameterProcessor.Plain)
            return value
        }

        val converter: KFunction<*>? = converters.getConverter(valueClazz)

        val processor: ParameterProcessor = when {
            // converterに一致する組み合わせが有れば設定されていればそれを使う
            converter != null -> ParameterProcessor.UseConverter(converter)
            // 要求された値がenumかつ元が文字列ならenum mapperでマップ
            javaClazz.isEnum && value is String -> ParameterProcessor.ToEnum(javaClazz)
            // 要求されているパラメータがStringならtoStringする
            clazz == String::class -> ParameterProcessor.ToString
            else -> throw IllegalArgumentException("Can not convert $valueClazz to $clazz")
        }
        convertCache.putIfAbsent(valueClazz, processor)
        return processor.process(value)
    }

    companion object {
        fun newInstance(param: KParameter): ParameterForMap<*> {
            return ParameterForMap(param, param.type.classifier as KClass<*>)
        }
    }
}

private sealed class ParameterProcessor {
    abstract fun process(value: Any): Any?

    object Plain : ParameterProcessor() {
        override fun process(value: Any): Any? = value
    }

    class UseConverter(private val converter: KFunction<*>) : ParameterProcessor() {
        override fun process(value: Any): Any? = converter.call(value)
    }

    class ToEnum(private val javaClazz: Class<*>) : ParameterProcessor() {
        override fun process(value: Any): Any? = EnumMapper.getEnum(javaClazz, value as String)
    }

    object ToString : ParameterProcessor() {
        override fun process(value: Any): Any? = value.toString()
    }
}

package com.mapk.kmapper

import com.mapk.core.EnumMapper
import com.mapk.core.ValueParameter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.isSuperclassOf

internal class ParameterForMap<T : Any>(
    param: ValueParameter<T>,
    private val parameterNameConverter: (String) -> String
) {
    val name: String = param.name
    private val clazz: KClass<T> = param.requiredClazz

    private val javaClazz: Class<T> by lazy {
        clazz.java
    }
    // リストの長さが小さいと期待されるためこの形で実装しているが、理想的にはmap的なものが使いたい
    @Suppress("UNCHECKED_CAST")
    private val converters: Set<Pair<KClass<*>, KFunction<T>>> by lazy {
        (param.getConverters() as Set<Pair<KClass<*>, KFunction<T>>>) + clazz.getConverters()
    }

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
            // 入力がmapもしくはpairなら、KMapperを用いてマッピングを試みる
            value is Map<*, *> || value is Pair<*, *> ->
                ParameterProcessor.UseKMapper(KMapper(clazz, parameterNameConverter))
            else -> ParameterProcessor.UseBoundKMapper(BoundKMapper(clazz, valueClazz, parameterNameConverter))
        }
        convertCache.putIfAbsent(valueClazz, processor)
        return processor.process(value)
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

    class UseKMapper(private val kMapper: KMapper<*>) : ParameterProcessor() {
        override fun process(value: Any): Any? = kMapper.map(value, PARAMETER_DUMMY)
    }

    @Suppress("UNCHECKED_CAST")
    class UseBoundKMapper<T : Any>(private val boundKMapper: BoundKMapper<T, *>) : ParameterProcessor() {
        override fun process(value: Any): Any? = boundKMapper.map(value as T)
    }

    class ToEnum(private val javaClazz: Class<*>) : ParameterProcessor() {
        override fun process(value: Any): Any? = EnumMapper.getEnum(javaClazz, value as String)
    }

    object ToString : ParameterProcessor() {
        override fun process(value: Any): Any? = value.toString()
    }
}

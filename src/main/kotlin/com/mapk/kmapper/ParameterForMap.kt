package com.mapk.kmapper

import com.mapk.core.EnumMapper
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

    private val convertCache: MutableMap<KClass<*>, (Any) -> Any?> = HashMap()

    fun <U : Any> mapObject(value: U): Any? {
        val valueClazz: KClass<*> = value::class

        // 取得方法のキャッシュが有ればそれを用いる
        convertCache[valueClazz]?.let { return it(value) }

        // パラメータに対してvalueが代入可能（同じもしくは親クラス）であればそのまま用いる
        if (clazz.isSuperclassOf(valueClazz)) {
            convertCache[valueClazz] = { value }
            return value
        }

        val converter: KFunction<*>? = converters.getConverter(valueClazz)

        val lambda: (Any) -> Any? = when {
            // converterに一致する組み合わせが有れば設定されていればそれを使う
            converter != null -> { { converter.call(it) } }
            // 要求された値がenumかつ元が文字列ならenum mapperでマップ
            javaClazz.isEnum && value is String -> { { EnumMapper.getEnum(javaClazz, it as String) } }
            // 要求されているパラメータがStringならtoStringする
            clazz == String::class -> { { it.toString() } }
            else -> throw IllegalArgumentException("Can not convert $valueClazz to $clazz")
        }
        convertCache[valueClazz] = lambda
        return lambda(value)
    }

    companion object {
        fun newInstance(param: KParameter): ParameterForMap<*> {
            return ParameterForMap(param, param.type.classifier as KClass<*>)
        }
    }
}

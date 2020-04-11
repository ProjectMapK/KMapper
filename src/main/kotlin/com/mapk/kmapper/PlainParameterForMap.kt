package com.mapk.kmapper

import com.mapk.core.EnumMapper
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.isSuperclassOf

internal class PlainParameterForMap<T : Any> private constructor(val param: KParameter, private val clazz: KClass<T>) {
    private val javaClazz: Class<T> by lazy {
        clazz.java
    }
    // リストの長さが小さいと期待されるためこの形で実装しているが、理想的にはmap的なものが使いたい
    private val converters: Set<Pair<KClass<*>, KFunction<T>>> = clazz.getConverters()

    fun <U : Any> mapObject(value: U): Any? {
        val valueClazz: KClass<*> = value::class

        // パラメータに対してvalueが代入可能（同じもしくは親クラス）であればそのまま用いる
        if (clazz.isSuperclassOf(valueClazz)) return value

        val converter: KFunction<*>? = getConverter(valueClazz)

        return when {
            // converterに一致する組み合わせが有れば設定されていればそれを使う
            converter != null -> converter.call(value)
            // 要求された値がenumかつ元が文字列ならenum mapperでマップ
            javaClazz.isEnum && value is String -> EnumMapper.getEnum(javaClazz, value)
            // 要求されているパラメータがStringならtoStringする
            clazz == String::class -> value.toString()
            else -> throw IllegalArgumentException("Can not convert $valueClazz to $clazz")
        }
    }

    // 引数の型がconverterに対して入力可能ならconverterを返す
    private fun <R : Any> getConverter(input: KClass<out R>): KFunction<T>? =
        converters.find { (key, _) -> input.isSubclassOf(key) }?.second

    companion object {
        fun newInstance(param: KParameter): PlainParameterForMap<*> {
            return PlainParameterForMap(param, param.type.classifier as KClass<*>)
        }
    }
}

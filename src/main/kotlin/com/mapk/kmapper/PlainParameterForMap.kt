package com.mapk.kmapper

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.isSubclassOf

internal class PlainParameterForMap<T : Any> private constructor(val param: KParameter, val clazz: KClass<T>) {
    val javaClazz: Class<T> by lazy {
        clazz.java
    }
    // リストの長さが小さいと期待されるためこの形で実装しているが、理想的にはmap的なものが使いたい
    private val converters: Set<Pair<KClass<*>, KFunction<T>>> by lazy {
        convertersFromConstructors(clazz) + convertersFromStaticMethods(clazz) + convertersFromCompanionObject(clazz)
    }

    // 引数の型がconverterに対して入力可能ならconverterを返す
    fun <R : Any> getConverter(input: KClass<out R>): KFunction<T>? =
        converters.find { (key, _) -> input.isSubclassOf(key) }?.second

    companion object {
        fun newInstance(param: KParameter): PlainParameterForMap<*> {
            return PlainParameterForMap(param, param.type.classifier as KClass<*>)
        }
    }
}

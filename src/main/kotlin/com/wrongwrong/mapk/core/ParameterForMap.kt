package com.wrongwrong.mapk.core

import com.wrongwrong.mapk.annotations.KPropertyAlias
import com.wrongwrong.mapk.annotations.KConverter
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.functions
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.staticFunctions
import kotlin.reflect.jvm.isAccessible

internal  class ParameterForMap<T: Any>(
    val param: KParameter, val clazz: KClass<T>, propertyNameConverter: (String) -> String
) {
    val name: String = param.annotations
        .find { it is KPropertyAlias }
        ?.let { (it as KPropertyAlias).value }
        ?: propertyNameConverter(param.name!!)

    val javaClazz: Class<T> by lazy {
        clazz.java
    }
    // リストの長さが小さいと期待されるためこの形で実装しているが、理想的にはmap的なものが使いたい
    private val creators: Set<Pair<KClass<*>, (Any) -> Any?>> by lazy {
        creatorsFromConstructors(clazz) + creatorsFromStaticMethods(clazz) + creatorsFromCompanionObject(clazz)
    }

    // 引数の型がcreatorに対して入力可能ならcreatorを返す
    fun <T: Any> getCreator(input: KClass<out T>): ((T) -> Any?)? =
        creators.find { (key, _) -> input.isSubclassOf(key) }?.let { (_, creator) -> creator }
}

private fun Collection<KFunction<*>>.getCreatorMapFromFunctions(): Set<Pair<KClass<*>, (Any) -> Any?>> {
    return filter { it.annotations.any { annotation -> annotation is KConverter } }
        .map { func ->
            func.isAccessible = true

            val call = { it: Any ->
                func.call(it)
            }

            (func.parameters.single { param -> param.kind == KParameter.Kind.VALUE }.type.classifier as KClass<*>) to
                    call
        }.toSet()
}

private fun creatorsFromConstructors(clazz: KClass<*>): Set<Pair<KClass<*>, (Any) -> Any?>> {
    return clazz.constructors.getCreatorMapFromFunctions()
}

private fun creatorsFromStaticMethods(clazz: KClass<*>): Set<Pair<KClass<*>, (Any) -> Any?>> {
    return clazz.staticFunctions.getCreatorMapFromFunctions()
}

private fun creatorsFromCompanionObject(clazz: KClass<*>): Set<Pair<KClass<*>, (Any) -> Any?>> {
    return clazz.companionObjectInstance?.let { companionObject ->
        companionObject::class.functions
            .filter { it.annotations.any { annotation -> annotation is KConverter } }
            .map { function ->
                function.isAccessible = true

                val params = function.parameters
                if (params.size != 2) {
                    throw IllegalArgumentException("This function is not compatible num of arguments.")
                }

                val func = { value: Any ->
                    function.call(companionObject, value)
                }

                (params.single { param -> param.kind == KParameter.Kind.VALUE }.type.classifier as KClass<*>) to func
            }.toSet()
    }?: emptySet()
}

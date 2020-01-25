package com.wrongwrong.mapk.core

import com.wrongwrong.mapk.annotations.SingleArgCreator
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.functions
import kotlin.reflect.full.staticFunctions

class ParameterForMap(val param: KParameter, propertyNameConverter: (String) -> String) {
    val clazz: KClass<*> = (param.type.classifier as KClass<*>)
    val name: String = propertyNameConverter(param.name!!)

    val creatorMap: Map<KClass<*>, (Any) -> Any?> by lazy {
        creatorsFromConstructors(clazz) + creatorsFromStaticMethods(clazz) + creatorsFromCompanionObject(clazz)
    }
}

private fun Collection<KFunction<*>>.getCreatorMapFromFunctions(): Map<KClass<*>, (Any) -> Any?> {
    return filter { it.annotations.any { annotation -> annotation is SingleArgCreator } }
        .associate { func ->
            val call = { it: Any ->
                func.call(it)
            }

            (func.parameters.single { param -> param.kind == KParameter.Kind.VALUE }.type.classifier as KClass<*>) to
                    call
        }
}

private fun creatorsFromConstructors(clazz: KClass<*>): Map<KClass<*>, (Any) -> Any?> {
    return clazz.constructors.getCreatorMapFromFunctions()
}

private fun creatorsFromStaticMethods(clazz: KClass<*>): Map<KClass<*>, (Any) -> Any?> {
    return clazz.staticFunctions.getCreatorMapFromFunctions()
}

private fun creatorsFromCompanionObject(clazz: KClass<*>): Map<KClass<*>, (Any) -> Any?> {
    return clazz.companionObjectInstance?.let { companionObject ->
        companionObject::class.functions
            .filter { it.annotations.any { annotation -> annotation is SingleArgCreator } }
            .associate { function ->
                val params = function.parameters
                if (params.size != 2) {
                    throw IllegalArgumentException("This function is not compatible num of arguments.")
                }

                val func = { value: Any ->
                    function.call(companionObject, value)
                }

                (params.single { param -> param.kind == KParameter.Kind.VALUE }.type.classifier as KClass<*>) to func
            }
    }?: emptyMap()
}

package com.mapk.kmapper

import com.mapk.annotations.KConverter
import com.mapk.core.KFunctionWithInstance
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.functions
import kotlin.reflect.full.staticFunctions
import kotlin.reflect.jvm.isAccessible

internal fun <T : Any> KClass<T>.getConverters(): Set<Pair<KClass<*>, KFunction<T>>> =
    convertersFromConstructors(this) + convertersFromStaticMethods(this) + convertersFromCompanionObject(this)

private fun <T> Collection<KFunction<T>>.getConverterMapFromFunctions(): Set<Pair<KClass<*>, KFunction<T>>> {
    return filter { it.annotations.any { annotation -> annotation is KConverter } }
        .map { func ->
            func.isAccessible = true

            (func.parameters.single().type.classifier as KClass<*>) to func
        }.toSet()
}

private fun <T : Any> convertersFromConstructors(clazz: KClass<T>): Set<Pair<KClass<*>, KFunction<T>>> {
    return clazz.constructors.getConverterMapFromFunctions()
}

@Suppress("UNCHECKED_CAST")
private fun <T : Any> convertersFromStaticMethods(clazz: KClass<T>): Set<Pair<KClass<*>, KFunction<T>>> {
    val staticFunctions: Collection<KFunction<T>> = clazz.staticFunctions as Collection<KFunction<T>>

    return staticFunctions.getConverterMapFromFunctions()
}

@Suppress("UNCHECKED_CAST")
private fun <T : Any> convertersFromCompanionObject(clazz: KClass<T>): Set<Pair<KClass<*>, KFunction<T>>> {
    return clazz.companionObjectInstance?.let { companionObject ->
        companionObject::class.functions
            .filter { it.annotations.any { annotation -> annotation is KConverter } }
            .map { function ->
                val func: KFunction<T> = KFunctionWithInstance(
                    function,
                    companionObject
                ) as KFunction<T>

                (func.parameters.single().type.classifier as KClass<*>) to func
            }.toSet()
    } ?: emptySet()
}

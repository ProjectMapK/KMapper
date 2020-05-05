package com.mapk.kmapper

import com.mapk.annotations.KConverter
import com.mapk.conversion.KConvertBy
import com.mapk.core.KFunctionWithInstance
import com.mapk.core.ValueParameter
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.staticFunctions
import kotlin.reflect.jvm.isAccessible

internal fun <T : Any> KClass<T>.getConverters(): Set<Pair<KClass<*>, KFunction<T>>> =
    convertersFromConstructors(this) + convertersFromStaticMethods(this) + convertersFromCompanionObject(this)

private fun <T> Collection<KFunction<T>>.getConvertersFromFunctions(): Set<Pair<KClass<*>, KFunction<T>>> {
    return filter { it.annotations.any { annotation -> annotation is KConverter } }
        .map { func ->
            func.isAccessible = true

            (func.parameters.single().type.classifier as KClass<*>) to func
        }.toSet()
}

private fun <T : Any> convertersFromConstructors(clazz: KClass<T>): Set<Pair<KClass<*>, KFunction<T>>> {
    return clazz.constructors.getConvertersFromFunctions()
}

@Suppress("UNCHECKED_CAST")
private fun <T : Any> convertersFromStaticMethods(clazz: KClass<T>): Set<Pair<KClass<*>, KFunction<T>>> {
    val staticFunctions: Collection<KFunction<T>> = clazz.staticFunctions as Collection<KFunction<T>>

    return staticFunctions.getConvertersFromFunctions()
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

@Suppress("UNCHECKED_CAST")
internal fun <T : Any> ValueParameter<T>.getConverters(): Set<Pair<KClass<*>, KFunction<*>>> {
    return annotations.mapNotNull { paramAnnotation ->
        paramAnnotation.annotationClass
            .findAnnotation<KConvertBy>()
            ?.converters
            ?.map { it.primaryConstructor!!.call(paramAnnotation) }
    }.flatten().map { (it.srcClass) to it::convert as KFunction<*> }.toSet()
}

// 引数の型がconverterに対して入力可能ならconverterを返す
internal fun <T : Any> Set<Pair<KClass<*>, KFunction<T>>>.getConverter(input: KClass<out T>): KFunction<T>? =
    this.find { (key, _) -> input.isSubclassOf(key) }?.second

// 再帰的マッピング時にKMapperでマップする場合、引数の数が1つだと正常にマッピングが機能しないため、2引数にするために用いるダミー
internal val PARAMETER_DUMMY = "" to null

package com.wrongwrong.mapk.core

import com.wrongwrong.mapk.annotations.SingleArgCreator
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.functions

class ParameterForMap(val param: KParameter, propertyNameConverter: (String) -> String) {
    val clazz: KClass<*> = (param.type.classifier as KClass<*>)
    val name: String = propertyNameConverter(param.name!!)

    val creatorMap: Map<KClass<*>, KFunction<*>>? by lazy {
        listOfNotNull(clazz.constructors, clazz.companionObject?.functions)
            .flatten()
            .filter { it.annotations.any { annotation -> annotation is SingleArgCreator } }
            .associateBy { (it.parameters.single().type.classifier as KClass<*>) }
    }
}

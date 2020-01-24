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

    val creator: KFunction<*>? by lazy {
        val creators: List<KFunction<*>> = listOfNotNull(clazz.constructors, clazz.companionObject?.functions)
            .flatten()
            .filter {
                it.annotations.any { annotation -> annotation is SingleArgCreator }
            }

        if (creators.isEmpty()) null else creators.single()
    }
}

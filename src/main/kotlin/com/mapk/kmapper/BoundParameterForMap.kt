package com.mapk.kmapper

import com.mapk.core.EnumMapper
import java.lang.IllegalArgumentException
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.javaGetter

@Suppress("UNCHECKED_CAST")
internal class BoundParameterForMap<S : Any>(val param: KParameter, property: KProperty1<S, *>) {
    val map: (S) -> Any?

    init {
        // ゲッターが無いならエラー
        val propertyGetter = property.javaGetter
            ?: throw IllegalArgumentException("${property.name} does not have getter.")
        propertyGetter.isAccessible = true

        val paramClazz = param.type.classifier as KClass<*>
        val propertyClazz = property.returnType.classifier as KClass<*>

        val converter = (convertersFromConstructors(paramClazz) +
                convertersFromStaticMethods(paramClazz) +
                convertersFromCompanionObject(paramClazz))
            .filter { (key, _) -> propertyClazz.isSubclassOf(key) }
            .let {
                if (1 < it.size) throw IllegalArgumentException("${param.name} has multiple converter. $it")

                it.singleOrNull()?.second
            }

        map = when {
            converter != null -> { { converter.call(propertyGetter.invoke(it)) } }
            paramClazz.isSubclassOf(propertyClazz) -> { { propertyGetter.invoke(it) } }
            paramClazz.java.isEnum && propertyClazz == String::class -> { {
                EnumMapper.getEnum(paramClazz.java, propertyGetter.invoke(it) as String)
            } }
            paramClazz == String::class -> { { propertyGetter.invoke(it).toString() } }
            else -> throw IllegalArgumentException("Can not convert $propertyClazz to $paramClazz")
        }
    }
}

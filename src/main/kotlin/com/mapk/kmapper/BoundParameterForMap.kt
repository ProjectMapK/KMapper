package com.mapk.kmapper

import java.lang.IllegalArgumentException
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.javaGetter

@Suppress("UNCHECKED_CAST")
internal class BoundParameterForMap<S : Any>(val param: KParameter, property: KProperty1<S, *>) {
    val map: (S) -> Any?

    init {
        // ゲッターが無いならエラー
        val propertyGetter = property.javaGetter
            ?: throw IllegalArgumentException("${property.name} does not have getter.")
        propertyGetter.isAccessible = true

        // TODO: コンバータその他への対応
        map = {
            propertyGetter.invoke(it)
        }
    }
}

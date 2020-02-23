package com.wrongwrong.mapk.core

import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.isAccessible

class KFunctionForCall<T>(private val function: KFunction<T>, instance: Any? = null) {
    val parameters: List<KParameter> = function.parameters
    private val originalArray: Array<Any?>
    val argumentArray: Array<Any?> get() = originalArray.copyOf()

    init {
        // この関数には確実にアクセスするためアクセシビリティ書き換え
        function.isAccessible = true
        originalArray = if (instance != null) {
            Array(parameters.size) { if (it == 0) instance else null }
        } else {
            Array(parameters.size) { null }
        }
    }

    fun call(arguments: Array<Any?>): T {
        return function.call(*arguments)
    }
}

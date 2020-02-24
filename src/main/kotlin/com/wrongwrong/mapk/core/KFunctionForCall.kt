package com.wrongwrong.mapk.core

import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.isAccessible

class KFunctionForCall<T>(private val function: KFunction<T>, instance: Any? = null) {
    val parameters: List<KParameter> = function.parameters
    private val originalArgumentBucket: List<Any?>
    val argumentBucket: Array<Any?> get() = originalArgumentBucket.toTypedArray()

    init {
        // この関数には確実にアクセスするためアクセシビリティ書き換え
        function.isAccessible = true
        originalArgumentBucket = if (instance != null) {
            List(parameters.size) { if (it == 0) instance else null }
        } else {
            List(parameters.size) { null }
        }
    }

    fun call(arguments: Array<Any?>): T {
        return function.call(*arguments)
    }
}

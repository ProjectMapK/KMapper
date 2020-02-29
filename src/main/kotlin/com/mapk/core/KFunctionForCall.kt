package com.mapk.core

import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.isAccessible

class KFunctionForCall<T>(private val function: KFunction<T>, instance: Any? = null) {
    val parameters: List<KParameter> = function.parameters
    private val originalArgumentBucket: ArgumentBucket

    fun getArgumentBucket(): ArgumentBucket = originalArgumentBucket.clone()

    init {
        // この関数には確実にアクセスするためアクセシビリティ書き換え
        function.isAccessible = true
        originalArgumentBucket = if (instance != null) {
            ArgumentBucket(
                Array(parameters.size) { if (it == 0) instance else null },
                1,
                generateSequence(1) { it.shl(1) }
                    .take(parameters.size)
                    .toList()
            )
        } else {
            ArgumentBucket(
                Array(parameters.size) { null },
                0,
                generateSequence(1) { it.shl(1) }
                    .take(parameters.size)
                    .toList()
            )
        }
    }

    fun call(argumentBucket: ArgumentBucket): T {
        return function.call(*argumentBucket.bucket)
    }
}

package com.wrongwrong.mapk.core

import kotlin.reflect.KFunction
import kotlin.reflect.jvm.isAccessible

class KFunctionForCall<T>(
    private val function: KFunction<T>,
    parameterSize: Int,
    instance: Any? = null
) {
    private val originalArray: Array<Any?>
    val argumentArray: Array<Any?> get() = originalArray.copyOf()

    init {
        // この関数には確実にアクセスするためアクセシビリティ書き換え
        function.isAccessible = true

        // 必要が有ればinstanceを先に、無ければ
        originalArray = Array(parameterSize) { if (it == 0 && instance != null) instance else null }
    }

    fun call(arguments: Array<Any?>): T = function.call(*arguments)
}

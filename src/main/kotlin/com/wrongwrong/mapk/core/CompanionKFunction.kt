package com.wrongwrong.mapk.core

import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.instanceParameter

internal class CompanionKFunction<T>(
    private val function: KFunction<T>, private val instance: Any
): KFunction<T> by function {
    private val instanceParam by lazy { mapOf(function.instanceParameter!! to instance) }

    override val parameters: List<KParameter> by lazy {
        function.parameters.filter { it.kind != KParameter.Kind.INSTANCE }
    }

    override fun call(vararg args: Any?): T = function.call(instance, *args)

    override fun callBy(args: Map<KParameter, Any?>): T = function.callBy(instanceParam + args)
}

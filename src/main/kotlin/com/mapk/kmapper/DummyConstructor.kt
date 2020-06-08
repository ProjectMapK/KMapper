@file:Suppress("FunctionName")

package com.mapk.kmapper

import com.mapk.kmapper.BoundKMapper as Bound
import com.mapk.kmapper.KMapper as Normal
import com.mapk.kmapper.PlainKMapper as Plain
import kotlin.reflect.KFunction

inline fun <reified S : Any, reified D : Any> BoundKMapper(
    noinline parameterNameConverter: ((String) -> String)? = null
) = Bound(D::class, S::class, parameterNameConverter)

inline fun <reified S : Any, D : Any> BoundKMapper(
    function: KFunction<D>,
    noinline parameterNameConverter: ((String) -> String)? = null
) = Bound(function, S::class, parameterNameConverter)

inline fun <reified T : Any> KMapper(noinline parameterNameConverter: ((String) -> String)? = null) =
    Normal(T::class, parameterNameConverter)

inline fun <reified T : Any> PlainKMapper(noinline parameterNameConverter: ((String) -> String)? = null) =
    Plain(T::class, parameterNameConverter)

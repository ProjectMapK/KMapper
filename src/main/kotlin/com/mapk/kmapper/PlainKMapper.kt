package com.mapk.kmapper

import com.mapk.annotations.KGetterAlias
import com.mapk.annotations.KGetterIgnore
import com.mapk.core.ArgumentAdaptor
import com.mapk.core.KFunctionForCall
import com.mapk.core.toKConstructor
import java.lang.reflect.Method
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaGetter

class PlainKMapper<T : Any> private constructor(
    private val function: KFunctionForCall<T>,
    parameterNameConverter: (String) -> String
) {
    constructor(function: KFunction<T>, parameterNameConverter: (String) -> String = { it }) : this(
        KFunctionForCall(function, parameterNameConverter), parameterNameConverter
    )

    constructor(clazz: KClass<T>, parameterNameConverter: (String) -> String = { it }) : this(
        clazz.toKConstructor(parameterNameConverter), parameterNameConverter
    )

    private val parameterMap: Map<String, PlainParameterForMap<*>> = function.requiredParameters.associate {
        it.name to PlainParameterForMap(it, parameterNameConverter)
    }

    private fun bindArguments(argumentAdaptor: ArgumentAdaptor, src: Any) {
        src::class.memberProperties.forEach outer@{ property ->
            // propertyが公開されていない場合は処理を行わない
            if (property.visibility != KVisibility.PUBLIC) return@outer

            // ゲッターが取れない場合は処理を行わない
            val javaGetter: Method = property.javaGetter ?: return@outer

            var alias: String? = null
            // NOTE: IgnoreとAliasが同時に指定されるようなパターンを考慮してaliasが取れてもbreakしていない
            javaGetter.annotations.forEach {
                if (it is KGetterIgnore) return@outer // ignoreされている場合は処理を行わない
                if (it is KGetterAlias) alias = it.value
            }
            alias = alias ?: property.name

            parameterMap[alias!!]?.let {
                // javaGetterを呼び出す方が高速
                javaGetter.isAccessible = true
                argumentAdaptor.putIfAbsent(alias!!) { javaGetter.invoke(src)?.let { value -> it.mapObject(value) } }
                // 終了判定
                if (argumentAdaptor.isFullInitialized()) return
            }
        }
    }

    private fun bindArguments(argumentAdaptor: ArgumentAdaptor, src: Map<*, *>) {
        src.forEach { (key, value) ->
            parameterMap[key]?.let { param ->
                // 取得した内容がnullでなければ適切にmapする
                argumentAdaptor.putIfAbsent(key as String) { value?.let { param.mapObject(value) } }
                // 終了判定
                if (argumentAdaptor.isFullInitialized()) return
            }
        }
    }

    private fun bindArguments(argumentBucket: ArgumentAdaptor, srcPair: Pair<*, *>) {
        val key = srcPair.first.toString()

        parameterMap[key]?.let {
            argumentBucket.putIfAbsent(key) { srcPair.second?.let { value -> it.mapObject(value) } }
        }
    }

    fun map(srcMap: Map<String, Any?>): T {
        val adaptor = function.getArgumentAdaptor()
        bindArguments(adaptor, srcMap)

        return function.call(adaptor)
    }

    fun map(srcPair: Pair<String, Any?>): T {
        val adaptor = function.getArgumentAdaptor()
        bindArguments(adaptor, srcPair)

        return function.call(adaptor)
    }

    fun map(src: Any): T {
        val adaptor = function.getArgumentAdaptor()
        bindArguments(adaptor, src)

        return function.call(adaptor)
    }

    fun map(vararg args: Any): T {
        val adaptor = function.getArgumentAdaptor()

        listOf(*args).forEach { arg ->
            when (arg) {
                is Map<*, *> -> bindArguments(adaptor, arg)
                is Pair<*, *> -> bindArguments(adaptor, arg)
                else -> bindArguments(adaptor, arg)
            }
        }

        return function.call(adaptor)
    }
}

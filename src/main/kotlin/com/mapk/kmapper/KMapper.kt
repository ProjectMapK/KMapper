package com.mapk.kmapper

import com.mapk.annotations.KGetterAlias
import com.mapk.annotations.KGetterIgnore
import com.mapk.core.ArgumentAdaptor
import com.mapk.core.KFunctionForCall
import com.mapk.core.toKConstructor
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaGetter

class KMapper<T : Any> private constructor(
    private val function: KFunctionForCall<T>,
    parameterNameConverter: (String) -> String
) {
    constructor(function: KFunction<T>, parameterNameConverter: (String) -> String = { it }) : this(
        KFunctionForCall(function, parameterNameConverter), parameterNameConverter
    )

    constructor(clazz: KClass<T>, parameterNameConverter: (String) -> String = { it }) : this(
        clazz.toKConstructor(parameterNameConverter), parameterNameConverter
    )

    private val parameterMap: Map<String, ParameterForMap<*>> = function.requiredParameters.associate {
        it.name to ParameterForMap(it, parameterNameConverter)
    }

    private val getCache: ConcurrentMap<KClass<*>, List<ArgumentBinder>> = ConcurrentHashMap()

    private fun bindArguments(argumentAdaptor: ArgumentAdaptor, src: Any) {
        val clazz = src::class

        // キャッシュヒットしたら登録した内容に沿って取得処理を行う
        getCache[clazz]?.let { getters ->
            // 取得対象フィールドは十分絞り込んでいると考えられるため、終了判定は行わない
            getters.forEach { it.bindArgument(src, argumentAdaptor) }
            return
        }

        val tempBinderArrayList = ArrayList<ArgumentBinder>()

        clazz.memberProperties.forEach outer@{ property ->
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

            parameterMap[alias ?: property.name]?.let { param ->
                javaGetter.isAccessible = true

                val binder = ArgumentBinder(param, javaGetter)

                binder.bindArgument(src, argumentAdaptor)
                tempBinderArrayList.add(binder)
                // キャッシュの整合性を保つため、ここでは終了判定を行わない
            }
        }
        getCache.putIfAbsent(clazz, tempBinderArrayList)
    }

    private fun bindArguments(argumentAdaptor: ArgumentAdaptor, src: Map<*, *>) {
        src.forEach { (key, value) ->
            parameterMap[key]?.let { param ->
                // 取得した内容がnullでなければ適切にmapする
                argumentAdaptor.putIfAbsent(param.name) { value?.let { param.mapObject(value) } }
                // 終了判定
                if (argumentAdaptor.isFullInitialized()) return
            }
        }
    }

    private fun bindArguments(argumentAdaptor: ArgumentAdaptor, srcPair: Pair<*, *>) {
        val key = srcPair.first.toString()

        parameterMap[key]?.let {
            argumentAdaptor.putIfAbsent(key) { srcPair.second?.let { value -> it.mapObject(value) } }
        }
    }

    fun map(srcMap: Map<String, Any?>): T {
        val adaptor: ArgumentAdaptor = function.getArgumentAdaptor()
        bindArguments(adaptor, srcMap)

        return function.call(adaptor)
    }

    fun map(srcPair: Pair<String, Any?>): T {
        val adaptor: ArgumentAdaptor = function.getArgumentAdaptor()
        bindArguments(adaptor, srcPair)

        return function.call(adaptor)
    }

    fun map(src: Any): T {
        val adaptor: ArgumentAdaptor = function.getArgumentAdaptor()
        bindArguments(adaptor, src)

        return function.call(adaptor)
    }

    fun map(vararg args: Any): T {
        val adaptor: ArgumentAdaptor = function.getArgumentAdaptor()

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

private class ArgumentBinder(private val param: ParameterForMap<*>, private val javaGetter: Method) {
    fun bindArgument(src: Any, adaptor: ArgumentAdaptor) {
        adaptor.putIfAbsent(param.name) {
            javaGetter.invoke(src)?.let { param.mapObject(it) }
        }
    }
}

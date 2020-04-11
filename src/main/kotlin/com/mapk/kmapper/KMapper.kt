package com.mapk.kmapper

import com.mapk.annotations.KGetterAlias
import com.mapk.annotations.KGetterIgnore
import com.mapk.core.ArgumentBucket
import com.mapk.core.KFunctionForCall
import com.mapk.core.getAliasOrName
import com.mapk.core.isUseDefaultArgument
import com.mapk.core.toKConstructor
import java.lang.reflect.Method
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaGetter

private typealias MethodCache = Pair<String, Method>
private typealias PropertyCache = MutableMap<KProperty1<*, *>, MethodCache>

class KMapper<T : Any> private constructor(
    private val function: KFunctionForCall<T>,
    parameterNameConverter: (String) -> String
) {
    constructor(function: KFunction<T>, parameterNameConverter: (String) -> String = { it }) : this(
        KFunctionForCall(function), parameterNameConverter
    )

    constructor(clazz: KClass<T>, parameterNameConverter: (String) -> String = { it }) : this(
        clazz.toKConstructor(), parameterNameConverter
    )

    private val propertyCache: PropertyCache = HashMap()
    private val parameterMap: Map<String, PlainParameterForMap<*>> = function.parameters
        .filter { it.kind != KParameter.Kind.INSTANCE && !it.isUseDefaultArgument() }
        .associate { (parameterNameConverter(it.getAliasOrName()!!)) to PlainParameterForMap.newInstance(it) }

    private fun bindArguments(argumentBucket: ArgumentBucket, src: Any) {
        src::class.memberProperties.forEach outer@{ property ->
            // キャッシュヒットした場合
            propertyCache[property]?.let { (name, getter) ->
                // 入ってないはずが無いのでgetValue
                val param = parameterMap.getValue(name)
                argumentBucket.putIfAbsent(param.param, getter.invoke(src)?.let { value -> param.mapObject(value) })
                // 終了判定
                if (argumentBucket.isInitialized) return
                return@outer
            }

            // propertyが公開されていない場合は処理を行わない
            if (property.visibility != KVisibility.PUBLIC) return@outer

            // ゲッターが取れない場合は処理を行わない
            val javaGetter: Method = property.javaGetter ?: return@outer

            var propertyName: String? = null
            // NOTE: IgnoreとAliasが同時に指定されるようなパターンを考慮してaliasが取れてもbreakしていない
            javaGetter.annotations.forEach {
                if (it is KGetterIgnore) return@outer // ignoreされている場合は処理を行わない
                if (it is KGetterAlias) propertyName = it.value
            }
            propertyName = propertyName ?: property.name

            parameterMap[propertyName!!]?.let {
                // javaGetterを呼び出す方が高速
                javaGetter.isAccessible = true
                argumentBucket.putIfAbsent(it.param, javaGetter.invoke(src)?.let { value -> it.mapObject(value) })
                // 終了判定
                if (argumentBucket.isInitialized) return
                // キャッシュ登録
                propertyCache[property] = propertyName!! to javaGetter
            }
        }
    }

    private fun bindArguments(argumentBucket: ArgumentBucket, src: Map<*, *>) {
        src.forEach { (key, value) ->
            parameterMap[key]?.let { param ->
                // 取得した内容がnullでなければ適切にmapする
                argumentBucket.putIfAbsent(param.param, value?.let { param.mapObject(value) })
                // 終了判定
                if (argumentBucket.isInitialized) return
            }
        }
    }

    private fun bindArguments(argumentBucket: ArgumentBucket, srcPair: Pair<*, *>) {
        parameterMap[srcPair.first.toString()]?.let {
            argumentBucket.putIfAbsent(it.param, srcPair.second?.let { value -> it.mapObject(value) })
        }
    }

    fun map(srcMap: Map<String, Any?>): T {
        val bucket: ArgumentBucket = function.getArgumentBucket()
        bindArguments(bucket, srcMap)

        return function.call(bucket)
    }

    fun map(srcPair: Pair<String, Any?>): T {
        val bucket: ArgumentBucket = function.getArgumentBucket()
        bindArguments(bucket, srcPair)

        return function.call(bucket)
    }

    fun map(src: Any): T {
        val bucket: ArgumentBucket = function.getArgumentBucket()
        bindArguments(bucket, src)

        return function.call(bucket)
    }

    fun map(vararg args: Any): T {
        val bucket: ArgumentBucket = function.getArgumentBucket()

        listOf(*args).forEach { arg ->
            when (arg) {
                is Map<*, *> -> bindArguments(bucket, arg)
                is Pair<*, *> -> bindArguments(bucket, arg)
                else -> bindArguments(bucket, arg)
            }
        }

        return function.call(bucket)
    }
}

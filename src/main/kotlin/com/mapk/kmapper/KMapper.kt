package com.mapk.kmapper

import com.mapk.annotations.KGetterAlias
import com.mapk.annotations.KGetterIgnore
import com.mapk.core.ArgumentBucket
import com.mapk.core.EnumMapper
import com.mapk.core.KFunctionForCall
import com.mapk.core.getAliasOrName
import com.mapk.core.isUseDefaultArgument
import com.mapk.core.toKConstructor
import java.lang.reflect.Method
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KVisibility
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaGetter

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

    private val parameterMap: Map<String, PlainParameterForMap<*>> = function.parameters
        .filter { it.kind != KParameter.Kind.INSTANCE && !it.isUseDefaultArgument() }
        .associate { (parameterNameConverter(it.getAliasOrName()!!)) to PlainParameterForMap.newInstance(it) }

    private fun bindArguments(argumentBucket: ArgumentBucket, src: Any) {
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

            parameterMap[alias ?: property.name]?.let {
                // javaGetterを呼び出す方が高速
                javaGetter.isAccessible = true
                argumentBucket.putIfAbsent(it.param, javaGetter.invoke(src)?.let { value -> mapObject(it, value) })
                // 終了判定
                if (argumentBucket.isInitialized) return
            }
        }
    }

    private fun bindArguments(argumentBucket: ArgumentBucket, src: Map<*, *>) {
        src.forEach { (key, value) ->
            parameterMap[key]?.let { param ->
                // 取得した内容がnullでなければ適切にmapする
                argumentBucket.putIfAbsent(param.param, value?.let { mapObject(param, it) })
                // 終了判定
                if (argumentBucket.isInitialized) return
            }
        }
    }

    private fun bindArguments(argumentBucket: ArgumentBucket, srcPair: Pair<*, *>) {
        parameterMap[srcPair.first.toString()]?.let {
            argumentBucket.putIfAbsent(it.param, srcPair.second?.let { value -> mapObject(it, value) })
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

private fun <T : Any, R : Any> mapObject(param: PlainParameterForMap<R>, value: T): Any? {
    val valueClazz: KClass<*> = value::class

    // パラメータに対してvalueが代入可能（同じもしくは親クラス）であればそのまま用いる
    if (param.clazz.isSuperclassOf(valueClazz)) return value

    val converter: KFunction<*>? = param.getConverter(valueClazz)

    return when {
        // converterに一致する組み合わせが有れば設定されていればそれを使う
        converter != null -> converter.call(value)
        // 要求された値がenumかつ元が文字列ならenum mapperでマップ
        param.javaClazz.isEnum && value is String -> EnumMapper.getEnum(param.clazz.java, value)
        // 要求されているパラメータがStringならtoStringする
        param.clazz == String::class -> value.toString()
        else -> throw IllegalArgumentException("Can not convert $valueClazz to ${param.clazz}")
    }
}

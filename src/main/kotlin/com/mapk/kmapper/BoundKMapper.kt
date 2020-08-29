package com.mapk.kmapper

import com.mapk.annotations.KGetterAlias
import com.mapk.annotations.KGetterIgnore
import com.mapk.core.KFunctionForCall
import com.mapk.core.toKConstructor
import java.lang.IllegalArgumentException
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty1
import kotlin.reflect.KVisibility
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.jvmName

class BoundKMapper<S : Any, D : Any> private constructor(
    private val function: KFunctionForCall<D>,
    src: KClass<S>,
    parameterNameConverter: ((String) -> String)?
) {
    constructor(function: KFunction<D>, src: KClass<S>, parameterNameConverter: ((String) -> String)? = null) : this(
        KFunctionForCall(function, parameterNameConverter), src, parameterNameConverter
    )

    constructor(clazz: KClass<D>, src: KClass<S>, parameterNameConverter: ((String) -> String)? = null) : this(
        clazz.toKConstructor(parameterNameConverter), src, parameterNameConverter
    )

    private val parameters: List<BoundParameterForMap<S>>

    init {
        val srcPropertiesMap: Map<String, KProperty1<S, *>> = src.memberProperties
            .filter {
                // アクセス可能かつignoreされてないもののみ抽出
                it.visibility == KVisibility.PUBLIC && it.getter.annotations.none { annotation -> annotation is KGetterIgnore }
            }.associateBy { it.getter.findAnnotation<KGetterAlias>()?.value ?: it.name }

        parameters = function.requiredParameters
            .mapNotNull {
                srcPropertiesMap[it.name]?.let { property ->
                    BoundParameterForMap.newInstance(it, property, parameterNameConverter)
                }.apply {
                    // 必須引数に対応するプロパティがsrcに定義されていない場合エラー
                    if (this == null && !it.isOptional)
                        throw IllegalArgumentException("Property ${it.name} is not declared in ${src.jvmName}.")
                }
            }
    }

    fun map(src: S): D {
        val adaptor = function.getArgumentAdaptor()

        parameters.forEach {
            adaptor.forcePut(it.name, it.map(src))
        }

        return function.call(adaptor)
    }
}

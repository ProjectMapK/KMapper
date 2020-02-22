package com.wrongwrong.mapk.core

import com.wrongwrong.mapk.annotations.KConstructor
import com.wrongwrong.mapk.annotations.KPropertyAlias
import com.wrongwrong.mapk.annotations.KPropertyIgnore
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty1
import kotlin.reflect.KVisibility
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible

class KMapper<T : Any>(private val function: KFunction<T>, propertyNameConverter: (String) -> String = { it }) {
    constructor(clazz: KClass<T>, propertyNameConverter: (String) -> String = { it }) : this(
        getTarget(clazz), propertyNameConverter
    )

    private val parameters: Set<ParameterForMap<*>> = function.parameters
        .map { ParameterForMap.newInstance(it, propertyNameConverter) }
        .toSet()

    private val parameterMap: Map<String, ParameterForMap<*>> = function.parameters
        .associate {
            val param = ParameterForMap.newInstance(it, propertyNameConverter)
            param.name to param
        }

    init {
        if (parameters.isEmpty()) throw IllegalArgumentException("This function is not require arguments.")

        // private関数に対してもマッピングできなければ何かと不都合があるため、accessibleは書き換える
        function.isAccessible = true
    }

    fun map(srcMap: Map<String, Any?>): T {
        return srcMap.entries.mapNotNull { (key, value) ->
            parameterMap[key]?.let { param ->
                // 取得した内容がnullでなければ適切にmapする
                param.param to value?.let { mapObject(param, it) }
            }
        }.let { function.callBy(it.toMap()) }
    }

    fun map(srcPair: Pair<String, Any?>): T = parameterMap.getValue(srcPair.first).let {
        function.callBy(mapOf(it.param to srcPair.second?.let { value -> mapObject(it, value) }))
    }

    fun map(src: Any): T = src::class.memberProperties.filterTargets().mapNotNull { property ->
        val getter = property.getAccessibleGetter()
        parameterMap[getter.findAnnotation<KPropertyAlias>()?.value ?: property.name]?.let {
            it.param to getter.call(src)?.let { value -> mapObject(it, value) }
        }
    }.let { function.callBy(it.toMap()) }

    fun map(vararg args: Any): T = listOf(*args).map { arg ->
        when (arg) {
            is Map<*, *> -> arg.entries.mapNotNull { (key, value) ->
                parameterMap[key]?.let { param ->
                    param.param to value?.let { mapObject(param, it) }
                }
            }
            is Pair<*, *> -> {
                val param = parameterMap.getValue(arg.first as String)
                listOf(param.param to arg.second?.let { mapObject(param, it) })
            }
            else -> arg::class.memberProperties.filterTargets().mapNotNull { property ->
                val getter = property.getAccessibleGetter()
                parameterMap[getter.findAnnotation<KPropertyAlias>()?.value ?: property.name]?.let {
                    it.param to getter.call(arg)?.let { value -> mapObject(it, value) }
                }
            }
        }
    }.flatten().let { function.callBy(it.toMap()) }
}

private fun Collection<KProperty1<*, *>>.filterTargets(): Collection<KProperty1<*, *>> {
    return filter {
        it.visibility == KVisibility.PUBLIC && it.annotations.none { annotation -> annotation is KPropertyIgnore }
    }
}

private fun KProperty1<*, *>.getAccessibleGetter(): KProperty1.Getter<*, *> {
    // アクセス制限の有るクラスではpublicなプロパティでもゲッターにアクセスできない場合が有るため、アクセス可能にして使う
    getter.isAccessible = true
    return getter
}

@Suppress("UNCHECKED_CAST")
internal fun <T : Any> getTarget(clazz: KClass<T>): KFunction<T> {
    val factoryConstructor: List<KFunction<T>> =
        clazz.companionObjectInstance?.let { companionObject ->
            companionObject::class.functions
                .filter { it.annotations.any { annotation -> annotation is KConstructor } }
                .map { KFunctionWithInstance(it, companionObject) as KFunction<T> }
        } ?: emptyList()

    val constructors: List<KFunction<T>> = factoryConstructor + clazz.constructors
        .filter { it.annotations.any { annotation -> annotation is KConstructor } }

    if (constructors.size == 1) return constructors.single()

    if (constructors.isEmpty()) return clazz.primaryConstructor!!

    throw IllegalArgumentException("Find multiple target.")
}

private fun <T : Any, R : Any> mapObject(param: ParameterForMap<R>, value: T): Any? {
    val valueClazz: KClass<*> = value::class
    val creator: KFunction<*>? by lazy {
        param.getCreator(valueClazz)
    }

    return when {
        // パラメータに対してvalueが代入可能（同じもしくは親クラス）であればそのまま用いる
        param.clazz.isSuperclassOf(valueClazz) -> value
        // creatorに一致する組み合わせが有れば設定されていればそれを使う
        creator != null -> creator!!.call(value)
        // 要求された値がenumかつ元が文字列ならenum mapperでマップ
        param.javaClazz.isEnum && value is String -> EnumMapper.getEnum(param.clazz.java, value)
        // 要求されているパラメータがStringならtoStringする
        param.clazz == String::class -> value.toString()
        else -> throw IllegalArgumentException("Can not convert $valueClazz to ${param.clazz}")
    }
}

package com.wrongwrong.mapk.core

import com.wrongwrong.mapk.annotations.KConstructor
import com.wrongwrong.mapk.annotations.KPropertyAlias
import com.wrongwrong.mapk.annotations.KPropertyIgnore
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.KVisibility
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible

class KMapper<T : Any>(function: KFunction<T>, propertyNameConverter: (String) -> String = { it }) {
    constructor(clazz: KClass<T>, propertyNameConverter: (String) -> String = { it }) : this(
        getTarget(clazz), propertyNameConverter
    )

    private val parameterMap: Map<String, ParameterForMap<*>>
    private val functionForCall: KFunctionForCall<T>

    init {
        val params = function.parameters
        functionForCall = KFunctionForCall(function, params.size) // TODO: 必要に応じてインスタンスを渡す

        parameterMap = params.filter { it.kind != KParameter.Kind.INSTANCE }.associate {
            (it.findAnnotation<KPropertyAlias>()?.value ?: propertyNameConverter(it.name!!)) to
                    ParameterForMap.newInstance(it)
        }

        if (parameterMap.isEmpty()) throw IllegalArgumentException("This function is not require arguments.")
    }

    fun map(srcMap: Map<String, Any?>): T {
        val array: Array<Any?> = functionForCall.argumentArray

        srcMap.forEach { (key, value) ->
            parameterMap[key]?.let { param ->
                // 取得した内容がnullでなければ適切にmapする
                array[param.param.index] = value?.let { mapObject(param, it) }
            }
        }

        return functionForCall.call(array)
    }

    fun map(srcPair: Pair<String, Any?>): T = parameterMap.getValue(srcPair.first).let {
        val array: Array<Any?> = functionForCall.argumentArray
        array[it.param.index] = srcPair.second?.let { value -> mapObject(it, value) }
        functionForCall.call(array)
    }

    fun map(src: Any): T {
        val array: Array<Any?> = functionForCall.argumentArray

        src::class.memberProperties.forEach { property ->
            if (property.visibility == KVisibility.PUBLIC && property.annotations.none { annotation -> annotation is KPropertyIgnore }) {
                val getter = property.getAccessibleGetter()
                parameterMap[getter.findAnnotation<KPropertyAlias>()?.value ?: property.name]?.let {
                    array[it.param.index] = getter.call(src)?.let { value -> mapObject(it, value) }
                }
            }
        }

        return functionForCall.call(array)
    }

    fun map(vararg args: Any): T {
        val array: Array<Any?> = functionForCall.argumentArray

        listOf(*args).forEach { arg ->
            when (arg) {
                is Map<*, *> -> arg.forEach { (key, value) ->
                    parameterMap[key]?.let { param ->
                        // 取得した内容がnullでなければ適切にmapする
                        array[param.param.index] = value?.let { mapObject(param, it) }
                    }
                }
                is Pair<*, *> -> parameterMap.getValue(arg.first as String).let {
                    array[it.param.index] = arg.second?.let { value -> mapObject(it, value) }
                }
                else -> arg::class.memberProperties.forEach { property ->
                    if (property.visibility == KVisibility.PUBLIC && property.annotations.none { annotation -> annotation is KPropertyIgnore }) {
                        val getter = property.getAccessibleGetter()
                        parameterMap[getter.findAnnotation<KPropertyAlias>()?.value ?: property.name]?.let {
                            array[it.param.index] = getter.call(arg)?.let { value -> mapObject(it, value) }
                        }
                    }
                }
            }
        }

        return functionForCall.call(array)
    }
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

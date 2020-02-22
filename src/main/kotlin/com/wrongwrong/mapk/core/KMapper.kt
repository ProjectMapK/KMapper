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

    init {
        if (parameters.isEmpty()) throw IllegalArgumentException("This function is not require arguments.")

        // private関数に対してもマッピングできなければ何かと不都合があるため、accessibleは書き換える
        function.isAccessible = true
    }

    fun map(srcMap: Map<String, Any?>): T {
        return parameters.associate {
            // 取得した内容がnullでなければ適切にmapする
            it.param to srcMap.getValue(it.name)?.let { value ->
                mapObject(it, value)
            }
        }.let { function.callBy(it) }
    }

    fun map(srcPair: Pair<String, Any?>): T = parameters
        .single { it.name == srcPair.first }
        .let {
            function.callBy(mapOf(it.param to srcPair.second?.let { value -> mapObject(it, value) }))
        }

    fun map(src: Any): T {
        val srcMap: Map<String, KProperty1.Getter<*, *>> =
            src::class.memberProperties.filterTargets().associate { property ->
                val getter = property.getAccessibleGetter()

                (getter.findAnnotation<KPropertyAlias>()?.value ?: property.name) to getter
            }

        return parameters.associate {
            // 取得した内容がnullでなければ適切にmapする
            it.param to srcMap.getValue(it.name).call(src)?.let { value ->
                mapObject(it, value)
            }
        }.let { function.callBy(it) }
    }

    fun map(vararg args: Any): T {
        val srcMap: Map<String, () -> Any?> = listOf(*args)
            .map { arg ->
                when (arg) {
                    is Map<*, *> -> arg.entries.associate { (key, value) ->
                        (key as String) to { value }
                    }
                    is Pair<*, *> -> mapOf(arg.first as String to { arg.second })
                    else -> {
                        arg::class.memberProperties.filterTargets().associate { property ->
                            val getter = property.getAccessibleGetter()

                            (getter.findAnnotation<KPropertyAlias>()?.value ?: property.name) to { getter.call(arg) }
                        }
                    }
                }
            }.reduce { acc, map ->
                acc + map
            }

        return parameters.associate {
            // 取得した内容がnullでなければ適切にmapする
            it.param to srcMap.getValue(it.name)()?.let { value ->
                mapObject(it, value)
            }
        }.let { function.callBy(it) }
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

package com.wrongwrong.mapk.core

import com.wrongwrong.mapk.annotations.PropertyAlias
import com.wrongwrong.mapk.annotations.PropertyIgnore
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.KVisibility
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.full.memberProperties

class KMapper<T: Any>(private val function: KFunction<T>, propertyNameConverter: (String) -> String = { it }) {
    private val parameters: Set<ParameterForMap>

    init {
        val params: List<KParameter> = function.parameters

        if (params.isEmpty()) throw IllegalArgumentException("This function is not require arguments.")

        parameters = params
            .map { ParameterForMap(it, propertyNameConverter) }
            .toSet()
    }

    fun map(srcMap: Map<String, Any?>): T {
        return parameters.associate {
            // 取得した内容がnullでなければ適切にmapする
            it.param to srcMap.getValue(it.name)?.let { value ->
                mapObject(it, value)
            }
        }.let { function.callBy(it) }
    }

    fun map(src: Any): T {
        val srcMap: Map<String, KProperty1.Getter<*, *>> =
            src::class.memberProperties.filterTargets().associate { property ->
                val getter = property.getter

                val key = getter.annotations
                    .find { it is PropertyAlias }
                    ?.let { (it as PropertyAlias).value }
                    ?: property.name

                key to getter
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
                            val getter = property.getter

                            val key = getter.annotations
                                .find { it is PropertyAlias }
                                ?.let { (it as PropertyAlias).value }
                                ?: property.name

                            key to { getter.call(arg) }
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
        it.visibility == KVisibility.PUBLIC && it.annotations.none { annotation -> annotation is PropertyIgnore }
    }
}

private fun mapObject(param: ParameterForMap, value: Any): Any? {
    val valueClazz: KClass<*> = value::class
    val creator: ((Any) -> Any?)? by lazy {
        param.getCreator(valueClazz)
    }

    return when {
        // パラメータに対してvalueが代入可能（同じもしくは親クラス）であればそのまま用いる
        param.clazz.isSuperclassOf(valueClazz) -> value
        // creatorに一致する組み合わせが有れば設定されていればそれを使う
        creator != null -> creator!!(value)
        // 要求された値がenumかつ元が文字列ならenum mapperでマップ
        param.javaClazz.isEnum && value is String -> EnumMapper.getEnum(param.clazz.java, value)
        // 要求されているパラメータがStringならtoStringする
        param.clazz == String::class -> value.toString()
        else -> throw IllegalArgumentException("Can not convert $valueClazz to ${param.clazz}")
    }
}

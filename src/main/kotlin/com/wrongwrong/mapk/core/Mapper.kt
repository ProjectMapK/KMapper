package com.wrongwrong.mapk.core

import kotlin.reflect.KFunction

class Mapper<T: Any>(private val function: KFunction<T>, propertyNameConverter: (String) -> String = { it }) {
    private val parameters: Set<ParameterForMap>

    init {
        val params = function.parameters

        if (params.isEmpty()) throw IllegalArgumentException("This function is not require arguments.")

        parameters = params
            .map { ParameterForMap(it, propertyNameConverter) }
            .toSet()
    }

    private fun mapObject(param: ParameterForMap, value: Any): Any? {
        if (param.creatorMap.contains(value::class)) {
            // creatorに一致する組み合わせが有れば設定されていればそれを使う
            return param.creatorMap.getValue(value::class)(value)
        } else if (param.javaClazz.isEnum && value is String) {
            // 文字列ならEnumにマップ
            return EnumMapper.getEnum(param.clazz.java, value)
        }

        // TODO: デフォルト値をどう扱う？ nullを入れたらデフォルトになるんだっけ？
        return null
    }

    fun map(srcMap: Map<String, Any?>): T {
        return parameters.associate {
            val value = srcMap[it.name]

            it.param to when {
                // 取得した内容に対して型が不一致であればマップする
                value != null && value::class != it.clazz -> mapObject(it, value)
                else -> value
            }
        }.let { function.callBy(it) }
    }
}

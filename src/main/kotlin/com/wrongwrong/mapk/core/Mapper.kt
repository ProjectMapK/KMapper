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
        return when {
            // creatorが設定されていればそれを使う
            param.creator != null -> param.creator!!.call(value)
            // 文字列ならEnumにマップ
            param.clazz is Enum<*> && value is String -> EnumMapper.getEnum(param.clazz.java, value)
            // TODO: デフォルト値をどう扱う？ nullを入れたらデフォルトになるんだっけ？
            else -> null
        }
    }

    fun map(obj: Any): T {
        if (obj is Map<*, *>) {
            return parameters.associate {
                val value = obj[it.name]

                if (value != null && value::class != it.clazz) {
                    TODO()
                }

                it.param to value
            }.let { function.callBy(it) }
        }

        TODO()
    }
}

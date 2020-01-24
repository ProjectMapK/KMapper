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
        if (param.creatorMap != null && param.creatorMap!!.contains(value::class)) {
            // creatorに一致する組み合わせが有れば設定されていればそれを使う
            return param.creatorMap!![value::class]?.call(value)
        } else if (param.clazz is Enum<*> && value is String) {
            // 文字列ならEnumにマップ
            return EnumMapper.getEnum(param.clazz.java, value)
        }

        // TODO: デフォルト値をどう扱う？ nullを入れたらデフォルトになるんだっけ？
        return null
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

package com.wrongwrong.mapk.annotations

@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.RUNTIME)
annotation class PropertyAlias(val value: String)

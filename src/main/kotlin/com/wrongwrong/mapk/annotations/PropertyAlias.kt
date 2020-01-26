package com.wrongwrong.mapk.annotations

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class PropertyAlias(val value: String)

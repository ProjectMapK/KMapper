package com.wrongwrong.mapk.annotations

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class PropertyAlias(val value: String)

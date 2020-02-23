package com.wrongwrong.mapk.annotations

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class KGetterAlias(val value: String)

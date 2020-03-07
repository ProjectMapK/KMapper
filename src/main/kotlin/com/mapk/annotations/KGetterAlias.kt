package com.mapk.annotations

@Target(AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.RUNTIME)
annotation class KGetterAlias(val value: String)

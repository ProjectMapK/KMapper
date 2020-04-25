package com.mapk.conversion

import kotlin.reflect.KClass

@Target(AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class KConvertBy(val converters: Array<KClass<out AbstractKConverter<*, *, *>>>)

abstract class AbstractKConverter<A : Annotation, S : Any, D : Any>(protected val annotation: A) {
    abstract val srcClass: KClass<S>
    abstract fun convert(source: S?): D?
}

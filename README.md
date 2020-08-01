[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![CircleCI](https://circleci.com/gh/ProjectMapK/KMapper.svg?style=svg)](https://circleci.com/gh/ProjectMapK/KMapper)
[![](https://jitci.com/gh/ProjectMapK/KMapper/svg)](https://jitci.com/gh/ProjectMapK/KMapper)
[![codecov](https://codecov.io/gh/ProjectMapK/KMapper/branch/master/graph/badge.svg)](https://codecov.io/gh/ProjectMapK/KMapper)

---

[日本語版](https://github.com/ProjectMapK/KMapper/blob/master/README.ja.md)

---

KMapper
====
`KMapper` is a mapper library for `Kotlin`, which provides the following features.

- `Bean mapping` with `Objects`, `Map`, and `Pair` as sources
- Flexible and safe mapping based on function calls with reflection.
- Richer features and thus more flexible and labor-saving mapping.

## Demo code
Here is a comparison between writing the mapping code by manually and using `KMapper`.

If you write it manually, the more arguments you have, the more complicated the description will be.  
However, by using `KMapper`, you can perform mapping without writing much code.

Also, no external configuration file is required.

```kotlin
// If you write manually.
val dst = Dst(
    param1 = src.param1,
    param2 = src.param2,
    param3 = src.param3,
    param4 = src.param4,
    param5 = src.param5,
    ...
)

// If you use KMapper
val dst = KMapper(::Dst).map(src)
```

You can specify not only one source, but also multiple objects, `Pair`, `Map`, etc.

```kotlin
val dst = KMapper(::Dst).map(
    "param1" to "value of param1",
    mapOf("param2" to 1, "param3" to 2L),
    src1,
    src2
)
```

## Installation
`KMapper` is published on JitPack.  
You can use this library on maven, gradle and any other build tools.  
Please see [here](https://jitpack.io/#ProjectMapK/KMapper/) for the introduction method.  

### Example on maven
**1. add repository reference for JitPack**

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

**2. add dependency**

```xml
<dependency>
    <groupId>com.github.ProjectMapK</groupId>
    <artifactId>KMapper</artifactId>
    <version>Tag</version>
</dependency>
```

## Principle of operation
The behavior of `KMapper` is as follows.

1. Get the `KFunction` to be called.
2. Analyze the `KFunction` and determine what arguments are needed and how to deserialize them.
3. Get the value for each argument from inputs and deserialize it. and call the `KFunction`.

`KMapper` performs the mapping by calling a `function`, so the result is a Subject to the constraints on the `argument` and `nullability`.  
That is, there is no runtime error due to breaking the `null` safety of `Kotlin`(The `null` safety on type arguments may be broken due to problems on the `Kotlin` side). 

Also, it supports the default arguments which are peculiar to `Kotlin`.

## Types of mapper classes
The project offers three types of mapper classes.

- `KMapper`
- `PlainKMapper`
- `BoundKMapper`

Here is a summary of the features and advantages of each.  
Also, the common features are explained using `KMapper` as an example.

### KMapper
The `KMapper` is a basic mapper class for this project.  
It is suitable for using the same instance of the class, since it is cached internally to speed up the mapping process.

### PlainKMapper
`PlainKMapper` is a mapper class from `KMapper` without caching.  
Although the performance is not as good as `KMapper` in case of multiple mappings, it is suitable for use as a disposable mapper because there is no overhead of cache processing.

### BoundKMapper
`BoundKMapper` is a mapping class for the case where only one source class is available.  
It is faster than `KMapper`.

## Initialization
`KMapper` can be initialized from `method reference(KFunction)` to be called or the `KClass` to be mapped.

The following is a summary of each initialization.  
However, some of the initialization of `BoundKMapper` are shown as examples simplified by a dummy constructor.

### Initialize from method reference(KFunction)
When the `primary constructor` is the target of a call, you can initialize it as follows.

```kotlin
data class Dst(
    foo: String,
    bar: String,
    baz: Int?,

    ...

)

// Get constructor reference
val dstConstructor: KFunction<Dst> = ::Dst

// KMapper
val kMapper: KMapper<Dst> = KMapper(dstConstructor)
// PlainKMapper
val plainMapper: PlainKMapper<Dst> = PlainKMapper(dstConstructor)
// BoundKMapper
val boundKMapper: BoundKMapper<Src, Dst> = BoundKMapper(dstConstructor)
```

### Initialize from KClass
The `KMapper` can also be initialized from the `KClass`.  
By default, the `primary constructor` is the target of the call.

```kotlin
data class Dst(...)

// KMapper
val kMapper: KMapper<Dst> = KMapper(Dst::class)
// PlainKMapper
val plainMapper: PlainKMapper<Dst> = PlainKMapper(Dst::class)
// BoundKMapper
val boundKMapper: BoundKMapper<Src, Dst> = BoundKMapper(Dst::class, Src::class)
```

By using a `dummy constructor` and omitting `generics`, you can also write as follows.

```kotlin
// KMapper
val kMapper: KMapper<Dst> = KMapper()
// PlainKMapper
val plainMapper: PlainKMapper<Dst> = PlainKMapper()
// BoundKMapper
val boundKMapper: BoundKMapper<Src, Dst> = BoundKMapper()
```

### Specifying the target of a call by KConstructor annotation
When initializing from the `KClass`, all mapper classes can specify the function to be called by the `KConstructor` annotation.

In the following example, the `secondary constructor` is called.

```kotlin
data class Dst(...) {
    @KConstructor
    constructor(...) : this(...)
}

val mapper: KMapper<Dst> = KMapper(Dst::class)
```

Similarly, the following example calls the factory method.

```kotlin
data class Dst(...) {
    companion object {
        @KConstructor
        fun factory(...): Dst {
            ...
        }
    }
}

val mapper: KMapper<Dst> = KMapper(Dst::class)
```

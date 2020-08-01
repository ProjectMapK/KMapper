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

## Detailed usage

### Converting values during mapping
In mapping, you may want to convert one input type to another.  
The `KMapper` provides a rich set of conversion features for such a situation.

However, this conversion can be performed under the following conditions.

- Input is not `null`.
  - If `null` is involved, it is recommended to combine the `KParameterRequireNonNull` annotation with the default argument.
- Input cannot be assigned directly to an argument.

#### Conversions available by default
Some of the conversion features are available without any special description.

##### 1-to-1 conversion (nested mapping)
If you can't use arguments as they are and no other transformation is possible, `KMapper` tries to do 1-to-1 mapping using the mapping class.  
This allows you to perform the following nested mappings by default.

```kotlin
data class InnerDst(val foo: Int, val bar: Int)
data class Dst(val param: InnerDst)

data class InnerSrc(val foo: Int, val bar: Int)
data class Src(val param: InnerSrc)

val src = Src(InnerSrc(1, 2))
val dst = KMapper(::Dst).map(src)

println(dst.param) // -> InnerDst(foo=1, bar=2)
```

###### Specifies the function used for the nested mapping
Nested mapping is performed by initializing `BoundKMapper` from the class.  
For this reason, you can specify the target of the call with the `KConstructor` annotation.

##### Other conversions

###### Conversion from String to Enum
If the input is a `String` and the argument is an `Enum`, an attempt is made to convert the input to an `Enum` with the corresponding `name`.

```kotlin
enum class FizzBuzz {
    Fizz, Buzz, FizzBuzz;
}

data class Dst(val fizzBuzz: FizzBuzz)

val dst = KMapper(::Dst).map("fizzBuzz" to "Fizz")
println(dst) // -> Dst(fizzBuzz=Fizz)
```

###### Conversion to String
If the argument is a `String`, the input is converted  by `toString` method.

#### Specifying the conversion method using the KConverter annotation
If you create your own class and can be initialized from a single argument, you can use the `KConverter` annotation.  
The `KConverter` annotation can be added to a `constructor` or a `factory method` defined in a `companion object`.

```kotlin
// Annotate the primary constructor
data class FooId @KConverter constructor(val id: Int)
```

```kotlin
// Annotate the secondary constructor
data class FooId(val id: Int) {
    @KConverter
    constructor(id: String) : this(id.toInt())
}
```

```kotlin
// Annotate the factory method
data class FooId(val id: Int) {
    companion object {
        @KConverter
        fun of(id: String): FooId = FooId(id.toInt())
    }
}
```

```kotlin
// If the fooId is given a KConverter, Dst can do the mapping successfully without doing anything.
data class Dst(
    fooId: FooId,
    bar: String,
    baz: Int?,

    ...

)
```

#### Conversion by creating your own custom deserialization annotations
If you cannot use `KConverter`, you can convert it by creating a custom conversion annotations and adding it to the parameter.

Custom conversion annotation is made by defining a pair of `conversion annotation` and `converter`.  
As an example, we will show how to create a `ZonedDateTimeConverter` that converts from `java.sql.Timestamp` or `java.time.Instant` to `ZonedDateTime` in the specified time zone.

##### Create conversion annotation
You can define a conversion annotation by adding `@Target(AnnotationTarget.VALUE_PARAMETER)`, `KConvertBy` annotation, and several other annotations.

The argument of the `KConvertBy` annotation passes the `KClass` of the converter described below.  
This converter should be defined for each source type.

Also, although this example defines an argument to the annotation, you can get the value of the annotation from the converter.

```kotlin
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@KConvertBy([TimestampToZonedDateTimeConverter::class, InstantToZonedDateTimeConverter::class])
annotation class ZonedDateTimeConverter(val zoneIdOf: String)
```

##### Create converter
You can define `converter` by inheriting `AbstractKConverter<A, S, D>`.  
Generics `A`,`S`,`D` have the following meanings.

- `A`: `conversion annotation` `Type`.
- `S`: Source `Type`.
- `D`: Destination `Type`.

Below is an example of a converter that converts from `java.sql.Timestamp` to `ZonedDateTime`.

```kotlin
class TimestampToZonedDateTimeConverter(
    annotation: ZonedDateTimeConverter
) : AbstractKConverter<ZonedDateTimeConverter, Timestamp, ZonedDateTime>(annotation) {
    private val timeZone = ZoneId.of(annotation.zoneIdOf)

    override val srcClass: KClass<Timestamp> = Timestamp::class

    override fun convert(source: Timestamp): ZonedDateTime = ZonedDateTime.of(source.toLocalDateTime(), timeZone)
}
```

The argument to the converter's `primary constructor` should only take a conversion annotation.  
This is called when `KMapper` is initialized.

As shown in the example, you can refer to the arguments defined in the annotation.

##### Using custom conversion annotations
The conversion annotation and the converter defined so far are written together as follows.  
`InstantToZonedDateTimeConverter` is a converter whose source is `java.time.Instant`.

```kotlin
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@KConvertBy([TimestampToZonedDateTimeConverter::class, InstantToZonedDateTimeConverter::class])
annotation class ZonedDateTimeConverter(val zoneIdOf: String)

class TimestampToZonedDateTimeConverter(
    annotation: ZonedDateTimeConverter
) : AbstractKConverter<ZonedDateTimeConverter, Timestamp, ZonedDateTime>(annotation) {
    private val timeZone = ZoneId.of(annotation.zoneIdOf)

    override val srcClass: KClass<Timestamp> = Timestamp::class

    override fun convert(source: Timestamp): ZonedDateTime = ZonedDateTime.of(source.toLocalDateTime(), timeZone)
}

class InstantToZonedDateTimeConverter(
    annotation: ZonedDateTimeConverter
) : AbstractKConverter<ZonedDateTimeConverter, Instant, ZonedDateTime>(annotation) {
    private val timeZone = ZoneId.of(annotation.zoneIdOf)

    override val srcClass: KClass<Instant> = Instant::class

    override fun convert(source: Instant): ZonedDateTime = ZonedDateTime.ofInstant(source, timeZone)
}
```

When this is given, it becomes as follows.

```kotlin
data class Dst(
    @ZonedDateTimeConverter("Asia/Tokyo")
    val t1: ZonedDateTime,
    @ZonedDateTimeConverter("-03:00")
    val t2: ZonedDateTime
)
```

#### Conversion from Multiple Arguments
The `KParameterFlatten` annotation allows you to perform a transformation that requires more than one argument.

```kotlin
data class InnerDst(val fooFoo: Int, val barBar: String)
data class Dst(val bazBaz: InnerDst, val quxQux: LocalDateTime)
```

To specify a field name as a prefix, give it as follows.  
The class specified with `KParameterFlatten` is initialized from the function or the `primary constructor` specified with the aforementioned `KConstructor` annotation.

```kotlin
data class InnerDst(val fooFoo: Int, val barBar: String)
data class Dst(
    @KParameterFlatten
    val bazBaz: InnerDst,
    val quxQux: LocalDateTime
)
data class Src(val bazBazFooBoo: Int, val bazBazBarBar: String, val quxQux: LocalDateTime)

// required 3 arguments that bazBazFooFoo, bazBazBarBar, quxQux
val mapper = KMapper(::Dst)
```

##### KParameterFlatten annotation options
The `KParameterFlatten` annotation has two options for handling argument names of the nested classes.

###### fieldNameToPrefix
By default, the `KParameterFlatten` annotation tries to find a match by prefixing the name of the argument with the name of the prefix.  
If you don't want to prefix the argument names, you can set the `fieldNameToPrefix` option to `false`.

```kotlin
data class InnerDst(val fooFoo: Int, val barBar: String)
data class Dst(
    @KParameterFlatten(fieldNameToPrefix = false)
    val bazBaz: InnerDst,
    val quxQux: LocalDateTime
)

// required 3 arguments that fooFoo, barBar, quxQux
val mapper = KMapper(::Dst)
```

If `fieldNameToPrefix = false` is specified, the `nameJoiner` option is ignored.

###### nameJoiner
The `nameJoiner` specifies how to join argument names and argument names.  
For example, if `Src` is `snake_case`, the following command is used.

```kotlin
data class InnerDst(val fooFoo: Int, val barBar: String)
data class Dst(
    @KParameterFlatten(nameJoiner = NameJoiner.Snake::class)
    val bazBaz: InnerDst,
    val quxQux: LocalDateTime
)

// required 3 arguments that baz_baz_foo_foo, baz_baz_bar_bar, qux_qux
val mapper = KMapper(::Dst) { /* some naming transformation process */ }
```

By default, `camelCase` is specified, and `snake_case` and `kebab-case` are also supported.  
You can also write your own by creating `object` which extends the `NameJoiner` class.

##### Use with other conversion methods
The `KParameterFlatten` annotation also works with all the conversion methods introduced so far.  
Also, the `KParameterFlatten` annotation can be used in any number of layers of nested objects.

### Set the argument names and field names used for mapping
By default, `KMapper` searches the source for a field whose name corresponds to the argument name.  
On the other hand, there are times when you want to use a different name for the argument name and the source.

In order to deal with such a situation, `KMapper` provides some functions to set the argument name and field name used during mapping.

#### Conversion of argument names
With `KMapper`, you can set the argument name conversion function at initialization.
It can handle situations where constant conversion is required, for example, the argument naming convention is camel case and the source naming convention is snake case.

```kotlin
data class Dst(
    fooFoo: String,
    barBar: String,
    bazBaz: Int?
)

val mapper: KMapper<Dst> = KMapper(::Dst) { fieldName: String ->
    /* some naming transformation process */
}

// For example, by passing a conversion function to the snake case, the following input can be handled
val dst = mapper.map(mapOf(
    "foo_foo" to "foo",
    "bar_bar" to "bar",
    "baz_baz" to 3
))
```

And, of course, any conversion process can be performed within the lambda.

##### Propagation of the argument name conversion process
The argument name conversion process is also reflected in the nested mapping.  
Also, the conversion is applied to the aliases specified with the `KParameterAlias` annotation described below.

##### The actual conversion process
Although `KMapper` does not provide naming transformation, some of the most popular libraries in your project may also provide it.  
Here is a sample code of `Jackson` and `Guava` that actually passes the "CamelCase -> SnakeCase" transformations.

##### Jackson
```kotlin
import com.fasterxml.jackson.databind.PropertyNamingStrategy

val parameterNameConverter: (String) -> String = PropertyNamingStrategy.SnakeCaseStrategy()::translate
val mapper: KMapper<Dst> = KMapper(::Dst, parameterNameConverter)
```

##### Guava
```kotlin
import com.google.common.base.CaseFormat

val parameterNameConverter: (String) -> String = { fieldName: String ->
    CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, fieldName)
}
val mapper: KMapper<Dst> = KMapper(::Dst, parameterNameConverter)
```

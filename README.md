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

## How to use
Published on JitPack.  
Please see [here](https://jitpack.io/#ProjectMapK/KMapper/) for the introduction method.  

## Usages
### From multiple resources
```kotlin
class Src1(val arg1: String, val arg2: String)
val src2: Map<String, Any?> = mapOf("arg3" to 1, "arg4" to 1.0)
val src3: Pair<String, Any?> = "arg5" to null

class Dst(
    val arg1: String,
    val arg2: String,
    val arg3: Int,
    val arg4: Double,
    val arg5: String?
)

val newInstance = KMapper(::Dst).map(src1, src2, src3)
```

### Set alias on map
#### for getter
```kotlin
class Src(@KGetterAlias("aliased") val str: String)

class Dst(val aliased: String)

val newInstance = KMapper(::Dst).map(src)
```

#### for parameter
```kotlin
class Src(val str: String)

class Dst(@param:KPropertyAlias("str") private val _src: String) {
  val src = _src.someArrangement
}

val newInstance = KMapper(::Dst).map(src)
```

### Convert parameter name
```kotlin
val srcMap = mapOf("snake_case" to "SnakeCase")

class Dst(val snakeCase: String)

val dst: Dst = KMapper(::DataClass) { camelToSnake(it) }.map(src)
```

### Map param to another class

```kotlin
class ConverterClass @KConverter constructor(val arg: String) {
  companion object {
    @KConverter
    fun fromInt(arg: Int): ConverterClass {
      return ConverterClass(arg.toString)
    }
  }
}

class Src(val arg1: String, val arg2: Int)

class Dst(val arg1: ConverterClass, val arg2: ConverterClass)

val newInstance = KMapper(::Dst).map(src)
```

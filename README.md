MapK
====
This is a Mapper Libraly like a `ModelMapper` for `Kotlin`.  
You can call `KFunction`(e.g. `constructor`) from `Object`.

```kotlin
// before
val dst = Dst(
  param1 = src.param1,
  param2 = src.param2,
  param3 = src.param3,
  param4 = src.param4,
  param5 = src.param5,
  ...
)

// after
val newInstance = Mapper(DataClass::class.primaryConstructor!!).map(src)
```

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
```

### Set alias on map
#### for getter
```kotlin
class Src(@get:PropertyAlias("aliased") val str: String)

class Dst(val aliased: String)
```

#### for parameter
```kotlin
class Src(val str: String)

class Dst(@param:PropertyAlias("str") private val _src: String) {
  val src = _src.someArrangement
}
```

### Convert parameter name
```kotlin
val srcMap = mapOf("snake_case" to "SnakeCase")

class Dst(val snakeCase: String)

val dst: Dst = Mapper(DataClass::class.primaryConstructor!!) { it.toSnakeCase }.map(src)
```

### Map param to another class

```kotlin
class CreatorClass @SingleArgCreator constructor(val arg: String) {
  companion object {
    @SingleArgCreator
    fun fromInt(arg: Int): CreatorClass {
      return CreatorClass(arg.toString)
    }
  }
}

class Src(val arg1: String, val arg2: Int)

class Dst(val arg1: CreatorClass, val arg2: CreatorClass)
```

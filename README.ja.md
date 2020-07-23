[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![CircleCI](https://circleci.com/gh/ProjectMapK/KMapper.svg?style=svg)](https://circleci.com/gh/ProjectMapK/KMapper)
[![](https://jitci.com/gh/ProjectMapK/KMapper/svg)](https://jitci.com/gh/ProjectMapK/KMapper)
[![codecov](https://codecov.io/gh/ProjectMapK/KMapper/branch/master/graph/badge.svg)](https://codecov.io/gh/ProjectMapK/KMapper)

KMapper
====
`KMapper`は`Kotlin`向けのマッパーライブラリであり、以下の機能を提供します。  

- オブジェクトや`Map`、`Pair`をソースとした`Bean`マッピング
- `Kotlin`のリフレクションを用いた関数呼び出しベースの安全なマッピング
- 豊富な機能による、より柔軟かつ労力の少ないマッピング

## デモコード
手動でマッピングコードを書いた場合と`KMapper`を用いた場合を比較します。  
手動で書く場合引数が多ければ多いほど記述がかさみますが、`KMapper`を用いることで殆どコードを書かずにマッピングを行えます。  
また、外部の設定ファイルは一切必要ありません。

```kotlin
// 手動でマッピングを行う場合
val dst = Dst(
    param1 = src.param1,
    param2 = src.param2,
    param3 = src.param3,
    param4 = src.param4,
    param5 = src.param5,
    ...
)

// KMapperを用いる場合
val dst = KMapper(::Dst).map(src)
```

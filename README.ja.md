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

## インストール方法
`KMapper`は`JitPack`にて公開しており、`Maven`や`Gradle`といったビルドツールから手軽に利用できます。  
各ツールでの正確なインストール方法については下記をご参照ください。

- [ProjectMapK / KMapper](https://jitpack.io/#ProjectMapK/KMapper)

### Mavenでのインストール方法
以下は`Maven`でのインストール例です。

**1. JitPackのリポジトリへの参照を追加する**

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

**2. dependencyを追加する**

```xml
<dependency>
    <groupId>com.github.ProjectMapK</groupId>
    <artifactId>KMapper</artifactId>
    <version>Tag</version>
</dependency>
```

## 動作原理
`KMapper`は以下のように動作します。

1. 呼び出し対象の`KFunction`を取り出す
2. `KFunction`を解析し、必要な引数とその取り出し方を決定する
3. 入力からそれぞれの引数に対応する値の取り出しを行い、`KFunction`を呼び出す

最終的にはコンストラクタや`companion object`に定義したファクトリーメソッドなどを呼び出してマッピングを行うため、結果は`Kotlin`上の引数・`nullability`等の制約に従います。  
つまり、`Kotlin`の`null`安全が壊れることによる実行時エラーは発生しません（ただし、型引数の`nullability`に関しては`null`安全が壊れる場合が有ります）。

また、`Kotlin`特有の機能であるデフォルト引数等にも対応しています。

## マッパークラスの種類について
このプロジェクトでは以下の3種類のマッパークラスを提供しています。

- `KMapper`
- `PlainKMapper`
- `BoundKMapper`

以下にそれぞれの特徴と使いどころをまとめます。  
また、これ以降共通の機能に関しては`KMapper`を例に説明を行います。

### KMapper
`KMapper`はこのプロジェクトの基本となるマッパークラスです。  
内部ではキャッシュを用いたマッピングの高速化などを行っているため、マッパーを使い回す形での利用に向きます。

### PlainKMapper
`PlainKMapper`は`KMapper`からキャッシュ機能を取り除いたマッパークラスです。  
複数回マッピングを行った場合の性能は`KMapper`に劣りますが、キャッシュ処理のオーバーヘッドが無いため、マッパーを使い捨てる形での利用に向きます。

### BoundKMapper
`BoundKMapper`はソースとなるクラスが1つに限定できる場合に利用できるマッピングクラスです。  
`KMapper`に比べ高速に動作します。

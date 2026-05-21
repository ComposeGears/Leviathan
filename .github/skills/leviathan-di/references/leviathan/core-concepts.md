# Core concepts

## `DIScope`

`DIScope` controls dependency lifetimes. Dependencies that are scope-aware register cleanup with `scope.onClose` and release cached values when `close()` is called.

```kotlin
val scope = DIScope()
val repo = AppModule.userRepository.injectedIn(scope)
scope.close()
```

Use `DIScope.GLOBAL` only for truly global values.

## `Dependency<T>`

`Dependency<T>` is a typed handle that resolves to `T` inside a scope.

```kotlin
interface Dependency<T> {
    fun injectedIn(scope: DIScope): T
}
```

Keep `Dependency<T>` as constructor defaults or function parameters to stay testable.

## `MutableDependency<T, P>`

A mutable dependency can change behavior at runtime.

```kotlin
object ConfigModule : Leviathan {
    val endpoint by mutableOf { "https://prod.api" }
}

ConfigModule.endpoint.provides { "https://staging.api" }
```

## DSL styles

The recommended style is to implement `Leviathan` for implicit DSL access:

```kotlin
object Module : Leviathan {
    val value by singleton { 1 }
}

class FeatureModule : Leviathan {
    val value by singleton { 1 }
}
```

Alternatively, use explicit `Leviathan.` prefix:

```kotlin
object Module {
    val value by Leviathan.singleton { 1 }
}
```

## Injection helper inside providers

Inside `singleton`, `instanceOf`, `factoryOf`, and `mutableOf`, use `inject(dependency)` to resolve other dependencies from the same active scope.




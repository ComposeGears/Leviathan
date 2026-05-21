# Recipe: Anti-patterns to avoid

## 1) Implicit global scope everywhere

Bad:

```kotlin
val repo = Module.repo.injectedIn(DIScope.GLOBAL)
```

Prefer explicit scopes so lifetimes are visible and disposable.

## 2) Pulling dependencies deep inside domain code

Bad:

```kotlin
class UseCase {
    fun run() = Module.repo.injectedIn(DIScope.GLOBAL).load()
}
```

Prefer receiving `Dependency<T>` or concrete dependencies as constructor parameters.

## 3) Long-lived mutable test overrides

Always restore mutable providers after each test to avoid test pollution.



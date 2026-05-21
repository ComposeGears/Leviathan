# Recipe: Factories and caching

Choose between per-scope caching and always-new objects.

```kotlin
class RequestId(val value: String)

object Module : Leviathan {
    val cachedInScope by factoryOf(cacheInScope = true) {
        RequestId("cached")
    }

    val alwaysNew by factoryOf(cacheInScope = false) {
        RequestId("new")
    }
}

fun verify() {
    val scope = DIScope()
    check(Module.cachedInScope.injectedIn(scope) === Module.cachedInScope.injectedIn(scope))
    check(Module.alwaysNew.injectedIn(scope) !== Module.alwaysNew.injectedIn(scope))
    scope.close()
}
```

Guideline:

- Use `cacheInScope = true` for values reused across one request/screen/session.
- Use `cacheInScope = false` for truly transient values.



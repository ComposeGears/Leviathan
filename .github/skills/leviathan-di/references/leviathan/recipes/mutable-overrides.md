# Recipe: Mutable overrides

Use `mutableOf` when runtime configuration must switch without rebuilding modules.

```kotlin
object RuntimeConfig : Leviathan {
    val baseUrl by mutableOf { "https://api.prod" }
}

fun enableStaging() {
    RuntimeConfig.baseUrl.provides { "https://api.staging" }
}

fun enableProd() {
    RuntimeConfig.baseUrl.provides { "https://api.prod" }
}
```

Use cases:

- environment switching
- debug toggles
- preview/demo mode

## Important: Already-injected values remain unchanged

When you call `provides { ... }` to swap the provider, **only future injections** get the new value. Any instances already obtained via `injectedIn(scope)` stay as they were:

```kotlin
object Config : Leviathan {
    val apiUrl by mutableOf { "https://api.v1" }
}

val scope = DIScope()
val url1 = Config.apiUrl.injectedIn(scope)  // "https://api.v1"

Config.apiUrl.provides { "https://api.v2" }

val url2 = Config.apiUrl.injectedIn(scope)  // "https://api.v2"
check(url1 == "https://api.v1")  // url1 unchanged
```

## Reactive updates with provider injection

If you need previously-injected code to react to changes, inject the **provider** (the lambda itself) rather than the final value:

```kotlin
object Config : Leviathan {
    val apiUrlProvider by mutableOf { { "https://api.v1" } }  // Returns a lambda
}

class ApiClient(val urlProvider: Dependency<() -> String> = Config.apiUrlProvider) {
    fun request() {
        val url = urlProvider.injectedIn(scope)()  // Call the lambda
        // Uses current URL each time
    }
}

Config.apiUrlProvider.provides { { "https://api.v2" } }
client.request()  // Uses new URL ✓
```



# Getting started with Leviathan

Leviathan is a lightweight service-locator DI library for Kotlin Multiplatform.

## Add dependencies

```kotlin
sourceSets {
    commonMain.dependencies {
        implementation("io.github.composegears:leviathan:<version>")
        implementation("io.github.composegears:leviathan-compose:<version>")
    }
}
```

## Define dependencies

```kotlin
import com.composegears.leviathan.Leviathan

class HttpClient
class UserRepository(val client: HttpClient)

object AppModule : Leviathan {
    val httpClient by singleton { HttpClient() }
    val userRepository by instanceOf { UserRepository(inject(httpClient)) }
}
```

## Inject dependencies

```kotlin
import com.composegears.leviathan.DIScope

fun foo() {
    val scope = DIScope()
    val repository = AppModule.userRepository.injectedIn(scope)
    scope.close()
}
```

## Pick declaration type

- `singleton { ... }`: one value for the life of the declaration holder.
- `instanceOf { ... }`: one shared value while at least one active scope uses it.
- `factoryOf(cacheInScope = true) { ... }`: one value per scope.
- `factoryOf(cacheInScope = false) { ... }`: new value on each access.
- `mutableOf { ... }`: provider that can be swapped at runtime.



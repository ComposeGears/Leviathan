# Recipe: Global scope usage

Use `DIScope.GLOBAL` for app-wide singletons that should never be cleaned up.

```kotlin
import com.composegears.leviathan.Leviathan
import com.composegears.leviathan.DIScope

object AppConfig
class Logger

object SharedModule : Leviathan {
    val logger by singleton { Logger() }
}

fun initializeApp() {
    // Reach logger once from global scope
    val logger = SharedModule.logger.injectedIn(DIScope.GLOBAL)
    logger.log("App initialized")
}

fun someFunction() {
    // Can access from anywhere
    val logger = SharedModule.logger.injectedIn(DIScope.GLOBAL)
    logger.log("Something happened")
}

fun otherFunction() {
    // Same instance everywhere
    val logger = SharedModule.logger.injectedIn(DIScope.GLOBAL)
}
```

**Warnings:**

- `DIScope.GLOBAL` never closes; use only for truly global singletons
- Instances are never released during app lifetime
- Can hide resource leaks if misused
- Prefer explicit scopes for request/screen/session lifetimes



# Recipe: Singleton — holder-lifetime dependency

Use `singleton` when a dependency must live as long as its declaring module object, regardless of which scopes open or close.

```kotlin
import com.composegears.leviathan.Leviathan
import com.composegears.leviathan.DIScope

class DatabaseConnection

object AppModule : Leviathan {
    val db by singleton { DatabaseConnection() }
}

fun example() {
    val scope1 = DIScope()
    val conn1 = AppModule.db.injectedIn(scope1)
    scope1.close()  // scope is gone, but db instance is NOT released

    val scope2 = DIScope()
    val conn2 = AppModule.db.injectedIn(scope2)
    
    // Same instance survives across scope boundaries
    check(conn1 === conn2)
    
    scope2.close()  // still NOT released; lives with AppModule
}
```

**Key behavior:**

- Instance is created once on first `injectedIn()` call (lazy)
- Scope closures do **not** destroy the instance
- The instance lives as long as the declaring object (`AppModule`) is alive
- Use for stable, long-lived infrastructure: database connections, HTTP clients, loggers

**Contrast with `instanceOf`:**

`instanceOf` tracks scopes and releases the instance when all scopes close. `singleton` ignores scope closures entirely — the instance is bound to the holder, not to any scope.



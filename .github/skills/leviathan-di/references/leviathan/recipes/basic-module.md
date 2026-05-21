# Recipe: Basic module

Declare all four dependency styles to understand lifecycle differences.

```kotlin
import com.composegears.leviathan.Leviathan
import com.composegears.leviathan.DIScope

class NetworkApi
class UserRepository(val api: NetworkApi)
class AnalyticsTracker(val tag: String)

interface Logger
class ConsoleLogger : Logger

object AppModule : Leviathan {
    // Singleton: one instance for the holder lifetime
    val api by singleton { 
        NetworkApi()
    }
    
    // Instance: shared while at least one scope is active
    val userRepo by instanceOf { 
        UserRepository(inject(api)) 
    }
    
    // Factory: new per scope (caching enabled by default)
    val tracker by factoryOf { 
        AnalyticsTracker("request")
    }
    
    // Factory: always new (never cached)
    val requestId by factoryOf(cacheInScope = false) {
        AnalyticsTracker(java.util.UUID.randomUUID().toString())
    }
    
    // Interface binding
    val logger by instanceOf<Logger> { 
        ConsoleLogger() 
    }
    
    // Mutable: swappable at runtime
    val baseUrl by mutableOf { 
        "https://api.prod" 
    }
}

// Usage
fun example() {
    val scope = DIScope()
    val repo = AppModule.userRepo.injectedIn(scope)
    val t1 = AppModule.tracker.injectedIn(scope)
    val t2 = AppModule.tracker.injectedIn(scope)
    
    check(repo !== null)
    check(t1 === t2)  // cached in same scope
    
    scope.close()
}
```




# Recipe: Dependency graphs with inject

Build complex dependency graphs by using `inject(...)` inside factory/instance blocks.

```kotlin
import com.composegears.leviathan.Leviathan
import com.composegears.leviathan.DIScope

class Database
class Logger
class Analytics(val logger: Logger, val db: Database)
class UserService(val analytics: Analytics, val db: Database)

object ServiceModule : Leviathan {
    val database by singleton { 
        Database() 
    }
    
    val logger by singleton { 
        Logger() 
    }
    
    val analytics by instanceOf { 
        Analytics(
            logger = inject(logger),
            db = inject(database)
        )
    }
    
    val userService by instanceOf { 
        UserService(
            analytics = inject(analytics),
            db = inject(database)
        )
    }
}

fun example() {
    val scope = DIScope()
    val service = ServiceModule.userService.injectedIn(scope)
    // All dependencies resolved transitively
    scope.close()
}
```

**Rules:**

- Use `inject(dependency)` to resolve other dependencies inside factory blocks
- Resolved instances are tied to the same scope
- For circular dependencies, see [Circular dependencies](circular-dependencies.md) (use lambdas to defer resolution)



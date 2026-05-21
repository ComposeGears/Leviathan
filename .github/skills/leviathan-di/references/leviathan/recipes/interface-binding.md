# Recipe: Interface binding and polymorphism

Bind implementations to interfaces using generic type parameters.

```kotlin
import com.composegears.leviathan.Leviathan
import com.composegears.leviathan.DIScope

interface UserRepository {
    fun getUser(id: String): User
}

class UserRepositoryImpl : UserRepository {
    override fun getUser(id: String) = User("impl-$id")
}

interface Logger {
    fun log(message: String)
}

class ConsoleLogger : Logger {
    override fun log(message: String) = println(message)
}

object ServiceModule : Leviathan {
    // Bind to interface type
    val userRepo by instanceOf<UserRepository> { 
        UserRepositoryImpl() 
    }
    
    val logger by instanceOf<Logger> { 
        ConsoleLogger() 
    }
}

fun example() {
    val scope = DIScope()
    
    // Access via interface type
    val repo: UserRepository = ServiceModule.userRepo.injectedIn(scope)
    val logger: Logger = ServiceModule.logger.injectedIn(scope)
    
    // Actual implementations are hidden
    assert(repo is UserRepositoryImpl)
    
    scope.close()
}
```

**Benefits:**

- Hide implementation details behind interfaces
- Swap implementations without changing injection sites
- Type-safe at compile time



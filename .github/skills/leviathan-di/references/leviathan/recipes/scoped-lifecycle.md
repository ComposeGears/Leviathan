# Recipe: Scoped lifecycle

Use `instanceOf` when objects should live only while at least one scope is active.

## Basic pattern

```kotlin
import com.composegears.leviathan.DIScope
import com.composegears.leviathan.Leviathan

class SessionState(val userId: String = "user123")

object SessionModule : Leviathan {
    val sessionState by instanceOf { SessionState() }
}

fun requestHandler() {
    val scope = DIScope()
    try {
        val state1 = SessionModule.sessionState.injectedIn(scope)
        val state2 = SessionModule.sessionState.injectedIn(scope)
        
        // Same instance within scope
        check(state1 === state2)
        
        doWork(state1)
    } finally {
        scope.close()  // Instance is released if no other scope uses it
    }
}
```

## Multi-scope sharing

```kotlin
fun multiScopeExample() {
    val scope1 = DIScope()
    val scope2 = DIScope()
    
    val state1 = SessionModule.sessionState.injectedIn(scope1)
    val state2 = SessionModule.sessionState.injectedIn(scope2)
    
    // Same instance; both scopes are tracked
    check(state1 === state2)
    
    scope1.close()  // One scope closes; instance stays alive
    val state3 = SessionModule.sessionState.injectedIn(scope2)
    check(state2 === state3)  // Still same instance
    
    scope2.close()  // All scopes closed; instance is released
    
    // Next access creates a new instance
    val state4 = SessionModule.sessionState.injectedIn(DIScope())
    check(state1 !== state4)
}
```

## Lifecycle

1. **Create scope** at flow entry point (request, screen, interaction)
2. **Inject dependencies** inside that scope multiple times → same instance
3. **Close scope** at flow exit → instance released
4. **Next scope** starts fresh with a new instance




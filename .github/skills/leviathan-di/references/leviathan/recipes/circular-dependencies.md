# Recipe: Circular dependencies with lambdas

Circular dependencies are supported by deferring injection with lambdas or functions.

```kotlin
import com.composegears.leviathan.Leviathan
import com.composegears.leviathan.DIScope

class ServiceA(val getB: () -> ServiceB)
class ServiceB(val getA: () -> ServiceA)

object Module : Leviathan {
    val serviceA by instanceOf { ServiceA { inject(serviceB) } }
    val serviceB by instanceOf { ServiceB { inject(serviceA) } }
}

fun example() {
    val scope = DIScope()
    val a = Module.serviceA.injectedIn(scope)
    val b = Module.serviceB.injectedIn(scope)
    
    // Call lambdas to access the other service
    val bFromA: ServiceB = a.getB()
    val aFromB: ServiceA = b.getA()
    
    check(bFromA === b)
    check(aFromB === a)
    
    scope.close()
}
```

**How it works:**

- `ServiceA` and `ServiceB` don't reference each other directly
- Instead, they hold **lambdas** that resolve the peer on demand
- The lambdas capture the current `DIScope` via `inject(...)`
- At resolution time, both `serviceA` and `serviceB` are already available in the scope
- Call the lambda to access the circular dependency

**Key:** Defer the actual injection with a function type, not eager constructor injection.


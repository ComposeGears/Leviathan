# Recipe: Compose integration

Use `leviathan-compose` helpers to bind DI lifetime to Compose lifecycle.

## Using `inject`

`inject` remembers the scope and dependency. Scope closes when composition is forgotten.

```kotlin
import androidx.compose.runtime.Composable
import com.composegears.leviathan.compose.inject

class FeedViewModel
class UserRepository

object FeedModule : Leviathan {
    val vm by instanceOf { FeedViewModel() }
    val repo by instanceOf { UserRepository() }
}

@Composable
fun FeedScreen() {
    val vm = inject(FeedModule.vm)
    val repo = inject(FeedModule.repo)
    
    LaunchedEffect(Unit) {
        repo.loadFeed()
    }
    
    FeedContent(vm)
}
```

## Using `injectAndRetain`

`injectAndRetain` keeps the scope and dependency alive as long as the **retention scope** (usually bound to screen attachment in the navigation backstack). The instance survives temporary recomposition and composition exit, but is released when the screen is removed from the navigation stack.

```kotlin
@Composable
fun FeedScreen() {
    // Scope lives until screen is popped from navigation backstack
    val vm = injectAndRetain(FeedModule.vm)
    val repo = injectAndRetain(FeedModule.repo)
    
    FeedContent(vm, repo)
}
```

**When to use `injectAndRetain`:**

- Expensive ViewModel instances that should survive config changes
- State that depends on screen attachment to the navigation backstack
- Avoiding re-initialization when recomposition happens during animations or transitions

## Behavior table

| Function | Scope lifetime | Recomposition | Navigation | Use case |
|---|---|---|---|---|
| `inject` | `remember` (composition) | recreate | close on exit | typical screens |
| `injectAndRetain` | `retain` (backstack) | survive | close on popped | expensive VMs, long-lived state |

**Key difference:** `inject` scope is tied to composition phases; `injectAndRetain` scope is tied to screen attachment in the navigation backstack.




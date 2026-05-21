# Recipe: ViewModel integration

Use `ViewModel.inject(...)` to keep a DI scope tied to the ViewModel lifecycle.

## Basic usage

```kotlin
import androidx.lifecycle.ViewModel
import com.composegears.leviathan.Dependency
import com.composegears.leviathan.compose.inject

class UserRepository
class FeedRepository

object Modules : Leviathan {
    val userRepo by instanceOf { UserRepository() }
    val feedRepo by instanceOf { FeedRepository() }
}

class FeedViewModel(
    private val userRepoDep: Dependency<UserRepository> = Modules.userRepo,
    private val feedRepoDep: Dependency<FeedRepository> = Modules.feedRepo
) : ViewModel() {
    
    fun loadFeed(): List<String> {
        val userRepo = inject(userRepoDep)
        val feedRepo = inject(feedRepoDep)
        return feedRepo.fetch()
    }
    
    override fun onCleared() {
        super.onCleared()
    }
}
```

## How it works

1. Each ViewModel gets an internal `DIScope` on first `inject()` call.
2. The scope is stored via `ViewModel.addCloseable()`.
3. When the ViewModel is cleared, the scope closes automatically.
4. All cached dependencies are released.

## Best practice

Keep `Dependency<T>` parameters in constructors with defaults:

- Production code gets real dependencies
- Test code injects fakes
- ViewModel lifetime is explicit





# Recipe: Constructor injection pattern

Keep your business logic testable by accepting `Dependency<T>` as constructor parameters with defaults.

## Pattern

```kotlin
import com.composegears.leviathan.Dependency
import com.composegears.leviathan.DIScope
import com.composegears.leviathan.Leviathan

class FeedRepository

object FeedModule : Leviathan {
    val repo by instanceOf { FeedRepository() }
}

class FeedViewModel(
    private val repoDep: Dependency<FeedRepository> = FeedModule.repo
) {
    private val scope = DIScope()

    fun load(): List<String> {
        val repo = repoDep.injectedIn(scope)
        return repo.fetchFeed()
    }

    fun clear() {
        scope.close()
    }
}
```

## Testing

```kotlin
class FakeFeedRepository : FeedRepository {
    fun fetchFeed() = listOf("fake-item")
}

class FeedViewModelTest {
    @Test
    fun loadFeed() {
        // Simple fake: use Leviathan.instanceOf or Leviathan.singleton
        val fakeRepo = Leviathan.instanceOf { FakeFeedRepository() }
        val vm = FeedViewModel(repoDep = fakeRepo)
        val result = vm.load()
        assert(result == listOf("fake-item"))
    }

    @Test
    fun loadFeedCountsOneInjection() {
        // When you need to track injection calls, implement Dependency directly
        var injectCount = 0
        val countingRepo = object : Dependency<FeedRepository> {
            override fun injectedIn(scope: DIScope): FeedRepository {
                injectCount++
                return FakeFeedRepository()
            }
        }
        val vm = FeedViewModel(repoDep = countingRepo)
        vm.load()
        assert(injectCount == 1)
    }
}
```

## Why this works

- **Default is production**: `repoDep` defaults to real module declaration
- **Overridable**: tests pass fake `Dependency<T>` directly
- **No global state**: scope ownership is visible in the class
- **No service locator calls**: no hidden lookup during testing





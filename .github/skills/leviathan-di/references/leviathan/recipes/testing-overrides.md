# Recipe: Testing overrides

Override mutable providers during tests and restore defaults in cleanup.

## Using mutable providers

```kotlin
import com.composegears.leviathan.Leviathan

interface HttpClient
class RealHttpClient : HttpClient
class FakeHttpClient : HttpClient

object NetworkModule : Leviathan {
    val httpClient by mutableOf<HttpClient> { RealHttpClient() }
}

class FeatureTest {
    @BeforeTest
    fun setUp() {
        NetworkModule.httpClient.provides { FakeHttpClient() }
    }

    @AfterTest
    fun tearDown() {
        NetworkModule.httpClient.provides { RealHttpClient() }
    }

    @Test
    fun testFeature() {
        val client = NetworkModule.httpClient.injectedIn(DIScope.GLOBAL)
        assert(client is FakeHttpClient)
    }
}
```

## Non-mutable declarations (preferred way)

For declarations you don't want to mutate, pass `Dependency<T>` through constructors.

For simple fakes, use `Leviathan.instanceOf` or `Leviathan.singleton`:

```kotlin
interface Api
class FakeApi : Api
class UserRepository(val apiDep: Dependency<Api> = RealModule.api)

class RepositoryTest {
    @Test
    fun testLoad() {
        val fakeApi = Leviathan.instanceOf<Api> { FakeApi() }
        val repo = UserRepository(apiDep = fakeApi)
        val result = repo.loadUser()
        assert(result.id == "fake-123")
    }
}
```

When you need more control (e.g. counting injection calls), implement `Dependency<T>` directly:

```kotlin
class RepositoryTest {
    @Test
    fun testInjectedOnce() {
        var callCount = 0
        val countingApi = object : Dependency<Api> {
            override fun injectedIn(scope: DIScope): Api {
                callCount++
                return FakeApi()
            }
        }
        val repo = UserRepository(apiDep = countingApi)
        repo.loadUser()
        assert(callCount == 1)
    }
}
```

## Best practices

- Keep mutable declarations in **test modules** only
- Restore defaults in `@AfterTest` to avoid test pollution
- Prefer constructor injection over mutables when possible
- Group mutable declarations in one testable module




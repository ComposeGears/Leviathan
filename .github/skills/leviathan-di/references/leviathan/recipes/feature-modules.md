# Recipe: Feature modules

Organize DI declarations by feature to avoid large central monolithic module and keep dependencies local.

## Structure

```kotlin
import com.composegears.leviathan.Leviathan

// auth/AuthModule.kt
object AuthModule : Leviathan {
    private val authApi by singleton { AuthApi() }
    val authRepo by instanceOf { AuthRepository(inject(authApi)) }
    val loginVm by factoryOf { LoginViewModel(inject(authRepo)) }
}

// feed/FeedModule.kt
object FeedModule : Leviathan {
    private val feedApi by singleton { FeedApi() }
    val feedRepo by instanceOf { FeedRepository(inject(feedApi)) }
    val feedVm by factoryOf { FeedViewModel(inject(feedRepo)) }
}

// shared/SharedModule.kt
object SharedModule : Leviathan {
    val appClock by singleton { Clock.System }
    val logger by singleton { Logger(inject(appClock)) }
}

// app/AppModule.kt (optional: compose root modules if needed)
object AppModule : Leviathan {
    val auth = AuthModule
    val feed = FeedModule
}
```

## Benefits

- **Decoupled** features: no need to know about other features' internals
- **Localized changes** avoid recompiling the entire module tree
- **Explicit dependencies** between features are visible at module level
- **Easy testing** each feature module can be tested in isolation





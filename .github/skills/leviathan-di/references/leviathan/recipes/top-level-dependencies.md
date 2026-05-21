# Recipe: Top-level dependencies

Top-level declarations (outside any object) are supported but **not recommended** — they are harder to discover, test, and organize:

```kotlin
// ⚠️ Use only when a dependency truly has no logical module owner
val appClock by Leviathan.singleton { Clock.System }
```

Prefer grouping declarations inside named module objects even for global concerns.

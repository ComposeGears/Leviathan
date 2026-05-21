---
name: leviathan-di
description: Learn and apply Leviathan, a service-locator style DI library for Kotlin Multiplatform. Includes practical recipes for scopes, factories, mutable providers, Compose, ViewModel, testing, and modularization.
metadata:
  author: ComposeGears
  last-updated: "2026-05-21"
  keywords:
    - DI
    - service locator
    - recipe
    - Kotlin
    - Kotlin Multiplatform
    - Leviathan
    - Compose
    - ViewModel
    - testing
    - modularization
---

## Getting started

- *[Getting started](references/leviathan/getting-started.md)*: Install Leviathan and define your first dependencies.
- *[Core concepts](references/leviathan/core-concepts.md)*: Understand `DIScope`, `Dependency`, `MutableDependency`, and lifecycle behavior.

## Recipes

### Base declarations

- *[Basic module](references/leviathan/recipes/basic-module.md)*: Declare singleton, instance, factory, and mutable dependencies.
- *[Constructor injection pattern](references/leviathan/recipes/constructor-injection-pattern.md)*: Keep constructors testable while resolving dependencies at boundaries.
- *[Top-level dependencies](references/leviathan/recipes/top-level-dependencies.md)*: Use app-wide dependencies and explicit scope control.

### Lifetime and caching

- *[Scoped lifecycle](references/leviathan/recipes/scoped-lifecycle.md)*: Model short-lived object graphs and release instances with scope closure.
- *[Factories and caching](references/leviathan/recipes/factories-and-caching.md)*: Choose per-scope cached factories vs always-new instances.
- *[Singleton: holder-lifetime dependency](references/leviathan/recipes/singleton-lifetime.md)*: Keep an instance alive for the life of the declaring module, regardless of scope closures.

### Dependency graphs

- *[Dependency graphs with inject](references/leviathan/recipes/dependency-graphs.md)*: Build complex wired dependencies using `inject(...)` inside factory blocks.
- *[Interface binding](references/leviathan/recipes/interface-binding.md)*: Bind implementations to interfaces for polymorphic injection.
- *[Circular dependencies](references/leviathan/recipes/circular-dependencies.md)*: Resolve cycles by deferring injection with lambdas.

### Runtime reconfiguration

- *[Mutable overrides](references/leviathan/recipes/mutable-overrides.md)*: Swap implementations at runtime for flags, environments, and previews.
- *[Testing overrides](references/leviathan/recipes/testing-overrides.md)*: Override providers safely in tests and restore defaults.

### Integrations

- *[Compose integration](references/leviathan/recipes/compose-integration.md)*: Inject dependencies in Composables with `inject` and `injectAndRetain`.
- *[ViewModel integration](references/leviathan/recipes/viewmodel-integration.md)*: Scope dependencies to `ViewModel` lifecycle.

### Architecture

- *[Feature modules](references/leviathan/recipes/feature-modules.md)*: Organize declarations by feature without centralized containers.
- *[Global scope usage](references/leviathan/recipes/global-scope.md)*: Use `DIScope.GLOBAL` for app-wide singletons.
- *[Anti-patterns](references/leviathan/recipes/anti-patterns.md)*: Avoid common service-locator pitfalls and hidden coupling.



<h1 align="center">Leviathan</h1>
<h2 align="center">Service locator implementation of DI pattern</h2>

<p align="center">
    <a target="_blank" href="https://github.com/ComposeGears/leviathan/stargazers"><img src="https://img.shields.io/github/stars/ComposeGears/leviathan.svg"></a>
    <a href="https://github.com/ComposeGears/leviathan/network"><img alt="API" src="https://img.shields.io/github/forks/ComposeGears/leviathan.svg"/></a>
    <a target="_blank" href="https://github.com/ComposeGears/leviathan/blob/main/LICENSE"><img src="https://img.shields.io/github/license/ComposeGears/leviathan.svg"></a>
    <a target="_blank" href="https://central.sonatype.com/artifact/io.github.composegears/leviathan"><img src="https://img.shields.io/maven-central/v/io.github.composegears/leviathan.svg?style=flat-square"/></a>
</p>


Add the dependency below to your **module**'s `build.gradle.kts` file:

| Module            |                                                                                                  Version                                                                                                  |
|-------------------|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|
| leviathan         |         [![Maven Central](https://img.shields.io/maven-central/v/io.github.composegears/leviathan.svg?style=flat-square)](https://central.sonatype.com/artifact/io.github.composegears/leviathan)         |
| leviathan-compose | [![Maven Central](https://img.shields.io/maven-central/v/io.github.composegears/leviathan-compose.svg?style=flat-square)](https://central.sonatype.com/artifact/io.github.composegears/leviathan-compose) |

#### Multiplatform

```kotlin
sourceSets {
    commonMain.dependencies {
        // core library
        implementation("io.github.composegears:leviathan:$version")
        // Compose integration 
        implementation("io.github.composegears:leviathan-compose:$version")
    }
}
```

#### Android / jvm

Use same dependencies in the `dependencies { ... }` section


Base usage
----------

Create `Module` (recommend to use `object`) and implement `Leviathan` marker interface.

Create fields using these functions:

- Use `by instanceOf { value }` to create a scoped instance dependency
  - the instance is shared while it belongs to at least one active scope
  - when the last scope closes, the instance is destroyed
- Use `by factoryOf(cacheInScope){ ... }` to create factory dependency
  - `cacheInScope = true` (default): caches instances within the same scopes
  - `cacheInScope = false`: creates new instance on each access
- Use `by singleton { value }` to create a constant dependency
- Use `by mutableOf { value }` to create a mutable provider dependency


Example
-----------

Declare your dependencies

```kotlin
class SampleRepository()
class SampleRepositoryWithParam(val param: Int)
class SampleRepositoryWithDependency(val dependency: SampleRepository)

interface SampleInterfaceRepo
class SampleInterfaceRepoImpl : SampleInterfaceRepo
```

Create module

```kotlin
// implicit DSL
object Module {
    val scopedRepository by Leviathan.instanceOf { SampleRepository() }
    val repositoryWithParam by Leviathan.factoryOf { SampleRepositoryWithParam(1) }
    val repositoryWithDependency by Leviathan.instanceOf {
        // dependency injection from the same module
        SampleRepositoryWithDependency(inject(scopedRepository)) 
    }
    // interface binding
    val interfaceRepo by Leviathan.instanceOf<SampleInterfaceRepo> { SampleInterfaceRepoImpl() }
    val constantValue by Leviathan.singleton { 42 }
    val mutableProvider by Leviathan.mutableOf { 53 }
}

// explicit DSL
class ModuleWithImplicitDsl : Leviathan {
    val scopedRepository by instanceOf { SampleRepository() }
    val repositoryWithDependency by instanceOf {
        SampleRepositoryWithDependency(inject(scopedRepository))
    }
}

// top-level
val appWideRepository by Leviathan.instanceOf { SampleRepository() }
```

Dependencies usage:

```kotlin
// view model
class SomeVM(
    dep1: Dependency<SampleRepository> = Module.scopedRepository,
) : ViewModel() {
    val dep1value = inject(dep1)

    fun foo(){
        val dep2 = inject(Module.interfaceRepo)
    }
}

// compose
@Composable
fun ComposeWithDI() {
    val repo1 = inject(Module.scopedRepository)
    val repo2 = inject { Module.repositoryWithParam }
    /*..*/
}

// random access
fun foo() {
    val scope = DIScope()
    val repo1 = Module.scopedRepository.injectedIn(scope)
    val repo2 = appWideRepository.injectedIn(scope)
    // update mutable values
    Module.mutableProvider.provides { 21 }
    /*..*/
    scope.close()
}
```

## Migration to 4.0.0

Before:

```kotlin
object Module : Leviathan() {
    val repo by instanceOf { SampleRepository() }
    val config by mutableOf { 1 }
}
```

After:

```kotlin
class ModuleWithDsl : Leviathan {
    val repo by instanceOf { SampleRepository() }
    val config by mutableOf { 1 }
}

// or

object Module {
    val repo by Leviathan.instanceOf { SampleRepository() }
    val config by Leviathan.mutableOf { 1 }
}
```

## Contributors

Thank you for your help! ❤️

<a href="https://github.com/ComposeGears/Leviathan/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=ComposeGears/Leviathan" />
</a>


# License
```
Developed by ComposeGears 2024

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

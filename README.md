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

Create `Module` (recommend to use `object`) and extends from `Leviathan` class

Create fields using these functions:

- Use `by instanceOf(keepAlive){ value }` to create instance dependency
  - `keepAlive = true` : instance persists across different scopes
  - `keepAlive = false`(default): instance is auto-closed when all scopes close
- Use `by factoryOf(useCache)` to create factory dependency
  - `useCache = true` (default): caches instances within the same scope
  - `useCache = false`: creates new instance on each access
- Use `by valueOf(value)` to create value dependency (the value may be updated later)
- Use `by providableOf { value }` to create a providable dependency (provider may be updated later)

All functions return a `Dependency<Type>` instance.


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
object Module : Leviathan() {
    val autoCloseRepository by instanceOf { SampleRepository() }
    val keepAliveRepository by instanceOf(keepAlive = true) { SampleRepository() }
    val repositoryWithParam by factoryOf { SampleRepositoryWithParam(1) }
    val repositoryWithDependency by instanceOf { 
        SampleRepositoryWithDependency(inject(autoCloseRepository)) 
    }
    val interfaceRepo by instanceOf<SampleInterfaceRepo> { SampleInterfaceRepoImpl() }
    val constantValue by valueOf(42)
    val mutableValue by mutableValueOf(87)
    val providable by providableOf { 53 }
}
```

Dependencies usage:

```kotlin
// view model
class SomeVM(
    dep1: Dependency<SampleRepository> = Module.autoCloseRepository,
) : ViewModel() {
    val dep1value = inject(dep1)

    fun foo(){
        val dep2 = inject(Module.interfaceRepo)
    }
}

// compose
@Composable
fun ComposeWithDI() {
    val repo1 = inject(Module.autoCloseRepository)
    val repo2 = inject { Module.repositoryWithParam }
    /*..*/
}

// random access
fun foo() {
    val scope = DIScope()
    val repo1 = Module.autoCloseRepository.injectedIn(scope)
    // update mutable values
    Module.mutableValue.provides(15)
    Module.providable.provides { 21 }
    /*..*/
    scope.close()
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

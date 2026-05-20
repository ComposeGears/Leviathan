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

- Use `by instanceOf { value }` to create a scoped instance dependency
  - the instance is shared while it belongs to at least one active scope
  - when the last scope closes, the instance is destroyed
- Use `by factoryOf(cacheInScope)` to create factory dependency
  - `cacheInScope = true` (default): caches instances within the same scope
  - `cacheInScope = false`: creates new instance on each access
- Use `by singleton(value)` to create a constant dependency
- Use `by mutableOf(value)` to create a mutable value dependency
- Use `by mutableOf { value }` to create a mutable provider dependency

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
    val scopedRepository by instanceOf { SampleRepository() }
    val repositoryWithParam by factoryOf { SampleRepositoryWithParam(1) }
    val repositoryWithDependency by instanceOf { 
        SampleRepositoryWithDependency(inject(scopedRepository)) 
    }
    val interfaceRepo by instanceOf<SampleInterfaceRepo> { SampleInterfaceRepoImpl() }
    val constantValue by singleton(42)
    val mutableValue by mutableOf(87)
    val mutableProvider by mutableOf { 53 }
}
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
    // update mutable values
    Module.mutableValue.provides(15)
    Module.mutableProvider.provides { 21 }
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

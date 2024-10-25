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

Create fields using one functions:

- Use `by instanceOf` to create single-object-delegate (same instance upon every access)
- Use `by lateInitInstance` to create instance-based late-init dependency (ps: you need to call `provides` method before
  access)
- Use `by factoryOf` to create factory-delegate (new instance upon each access)

Both functions return a dependency provider instance and the type of field will be `Dependency<Type>`

To retreive dependency use either `Module.dependency.get()` or define a property `val dep by Module.dependency`

Simple case
-----------

Declare you dependencies

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
    val lazyRepository by instanceOf(::SampleRepository)
    val nonLazyRepository by instanceOf(false, ::SampleRepository)
    val repositoryWithParam by factoryOf { SampleRepositoryWithParam(1) }
    val repositoryWithDependency by instanceOf { SampleRepositoryWithDependency(lazyRepository.get()) }
    val interfaceRepo by instanceOf<SampleInterfaceRepo>(::SampleInterfaceRepoImpl)
}
```

Dependencies usage:

```kotlin
fun foo() {
    val repo = Module.lazyRepository.get()
    //...  
}

class Model(
    val dep1: SampleRepository = Module.lazyRepository.get()
) {
    val dep2: SampleRepository by Module.nonLazyRepository
    //...
}

```

Mutli-module case
-----------------

Interface based approach

```kotlin
// ----------Module 1-------------
//Dependency 
class Dep {
    fun foo() {}
}

// ----------Module 2-------------
// Dependency provider interface
interface ICore {
    val dep: Dependency<Dep>
}

// Dependency provider implementation
internal class CoreImpl : Leviathan(), ICore {
    override val dep by instanceOf { Dep() }
}
// Dependency provider accessor
val Core: ICore = CoreImpl()

// ----------Module 3-------------    
// Usage
fun boo() {
    val dep by Core.dep
} 
   ```

Simple approach

```kotlin
// ----------Module 1-------------
//Dependency
class Dep {
    fun foo() {}
}

// ----------Module 2-------------
// Dependency provider & accessor
object Core : Leviathan() {
    val dep by instanceOf { Dep() }
}

// ----------Module 3-------------
// Usage
fun boo() {
    val dep by Core.dep
}
```

Advanced case
-------------

In order to create good & testable classes recommend to use advanced scenario

1) declare dependencies
    ```kotlin
    class DataRepository //...
    class ApiRepository //...
    ```
2) declare module interface (data/domain modules)
    ```kotlin
    interface DataModule {
        val dataRepository: Dependency<DataRepository>
    }
    
    interface ApiModule {
        val apiRepository: Dependency<ApiRepository>
    }    
    ```
3) Create `AppModule` and inherit from interfaces(step #2) and `Leviathan`
    ```kotlin
    object AppModule : DataModule, ApiModule, Leviathan() {
        override val dataRepository: Dependency<DataRepository> by instance(::DataRepository)
        override val apiRepository: Dependency<ApiRepository> by instance(::ApiRepository)
    }
    ```
4) Create Models (or any other classes) base on interfaces from step #2
    ```kotlin
    class Model(apiModule: ApiModule = AppModule){
        val api: ApiRepository by apiModule.apiRepository
   
        fun foo(){/*...*/}
    }
    ```

Now you can make tests and have easy way to mock your data:

```kotlin
@Test
fun ModelTests() {
    val model = Model(object : Leviathan(), ApiModule {
        override val apiRepository by instanceOf { MyMockApiRepository() }
    })
    model.foo()

    //-----or-----------

    AppModule.apiRepository.overrideWith { MyMockApiRepository() }
    val model = Model()
    model.foo()
}
```

Compose
-------------

Dependencies access in compose code:
```kotlin
class Repository(){
    fun foo(){}
}

object Module : Leviathan(){
    val dependency by instanceOf { Repository() }
}

@Composable
fun SomeComposable(){
    val dependency = leviathanInject { Module.dependency }
    ///...
}
```

# License

```xml
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

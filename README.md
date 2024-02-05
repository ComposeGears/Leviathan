Leviathan
=========

`Leviathan` is a service locator implementation of DI pattern 

build.gradle.kts

```kotlin
implementation("com.compose.gears.di.leviathan:leviathan:0.0.1")
```

Base usage
----------

Create `Module` (recommend to use `object`) and extends from `Leviathan` class

Create fields using one of 2 functions:
- Use `by instance` to create access single object (same instance upon every access)
- Use `by factory` to create factory objects (new instance upon each access)

Use `by (instance|factory)<Interface>{InterfaceImpl}` to hide impl class

Simple case
-----------

Declare you repositories

```kotlin
class SampleRepository()
class SampleRepositoryWithParam(val param: Int)
class SampleRepositoryWithDependency(val dependency: SampleRepository)

interface SampleInterfaceRepo
class SampleInterfaceRepoImpl : SampleInterfaceRepo
```
Create module

```kotlin
class Module : Leviathan() {
    val lazyRepository by instance(::SampleRepository)
    val nonLazyRepository by instance(false, ::SampleRepository)
    val repositoryWithParam by factory { SampleRepositoryWithParam(1) }
    val repositoryWithDependency by instance { SampleRepositoryWithDependency(lazyRepository) }
    val interfaceRepo by instance<SampleInterfaceRepo>(::SampleInterfaceRepoImpl)
}
```

Access declared dependencies

```kotlin
fun foo(){
    val repo = Module.lazyRepository
    //...  
}

class Model(
    val repo: SampleRepository = Module.lazyRepository
){
    //...
}

class Model(){
    private val repo = Module.lazyRepository
    //...
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
        val dataRepository: DataRepository
    }
    
    interface ApiModule {
        val apiRepository: ApiRepository
    }    
    ```
3) Create `AppModule` and inherit from interfaces(step #2) and `Leviathan`
    ```kotlin
    object AppModule : DataModule, ApiModule, Leviathan() {
        override val dataRepository: DataRepository by instance(::DataRepository)
        override val apiRepository: ApiRepository by instance(::ApiRepository)
    }
    ```
4) Create Models (or any other classes) base on interfaces from step #2
    ```kotlin
    class Model(apiModule: ApiModule = AppModule){
        val api = apiModule.apiRepository
   
        fun foo(){/*...*/}
    }
    ```
   
Now you can make tests and have easy way to mock your data:

```kotlin
@Test
fun ModelTests(){
    val model = Model(object : ApiModule {
        override val apiRepository: ApiRepository
            get() = ApiRepository() // mock
    })
    model.foo()
}
```
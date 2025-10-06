package com.composegears.leviathan

import kotlin.jvm.JvmStatic
import kotlin.reflect.KProperty

// ---  Scope definition -------------------------------------------

/**
 * A scope for managing the lifecycle of dependencies.
 */
public open class DIScope {
    public companion object {
        public val GLOBAL: DIScope = object : DIScope() {
            override fun onClose(action: () -> Unit) = Unit
        }
    }

    private val closeActions = mutableListOf<() -> Unit>()

    internal open fun onClose(action: () -> Unit) {
        closeActions += action
    }

    /**
     * Closes the scope and triggers all registered close actions.
     */
    public fun close() {
        closeActions.onEach { it() }
        closeActions.clear()
    }
}

public class DependencyInitializationScope internal constructor(
    private val diScope: DIScope
) {
    public fun <T> inject(dependency: Dependency<T>): T = dependency.injectedIn(diScope)
}

// ---  Dependency definition --------------------------------------

public interface Dependency<T> {
    public fun injectedIn(scope: DIScope): T
}

/**
 * A dependency that always provides the same value.
 * The value can be updated using the [provides] method.
 */
public class ValueDependency<T> internal constructor(
    private var value: T
) : Dependency<T> {
    override fun injectedIn(scope: DIScope): T = value

    /**
     * Updates the value of the dependency.
     */
    public fun provides(newValue: T) {
        value = newValue
    }
}

/**
 * A dependency that provides a value using a provider function.
 * The provider function can be updated to supply a new value.
 */
public class ProvidableDependency<T> internal constructor(
    private var provider: () -> T
) : Dependency<T> {
    override fun injectedIn(scope: DIScope): T = provider()

    /**
     * Updates the provider function to supply a new value.
     */
    public fun provides(valueProvider: () -> T) {
        provider = valueProvider
    }
}

/**
 * A dependency that provides a new instance using a factory function.
 * If [useCache] is true, the same instance will be provided every time the dependency
 * is injected within the same [DIScope]. If false, a new instance will be created every time.
 */
public class FactoryDependency<T> internal constructor(
    private val useCache: Boolean,
    private val factory: DependencyInitializationScope.() -> T
) : Dependency<T> {
    private val scopedCache = mutableMapOf<DIScope, T>()
    override fun injectedIn(scope: DIScope): T =
        if (useCache) {
            scopedCache.getOrElse(scope) {
                val instance = factory(DependencyInitializationScope(scope))
                scopedCache[scope] = instance
                scope.onClose {
                    scopedCache.remove(scope)
                }
                instance
            }
        } else factory(DependencyInitializationScope(scope))
}

/**
 * A dependency that provides a singleton instance using a factory function.
 * If [keepAlive] is true, the instance will be kept alive after being injected at least once.
 * If false, the instance will be destroyed as soon as the last [DIScope] using it is closed.
 */
public class InstanceDependency<T> internal constructor(
    private val keepAlive: Boolean,
    private val factory: DependencyInitializationScope.() -> T
) : Dependency<T> {

    private var scopes = mutableSetOf<DIScope>()
    private var instance: T? = null

    override fun injectedIn(scope: DIScope): T {
        if (!keepAlive && scope !in scopes) {
            scopes.add(scope)
            scope.onClose {
                scopes.remove(scope)
                if (scopes.isEmpty()) {
                    instance = null
                }
            }
        }
        if (instance == null) {
            instance = factory(DependencyInitializationScope(scope))
        }
        return instance!!
    }
}

// ---  Module definition ------------------------------------------

/**
 * Base class for defining a module of dependencies.
 * Extend this class and use [valueOf], [providableOf], [factoryOf] and [instanceOf] to define dependencies.
 *
 * Example:
 * ```
 * // ----- Module definition -----
 *
 * object Module : Leviathan() {
 *     val autoCloseRepository by instanceOf { SampleRepository() }
 *     val keepAliveRepository by instanceOf(keepAlive = true) { SampleRepository() }
 *     val repositoryWithParam by factoryOf { SampleRepositoryWithParam(1) }
 *     val repositoryWithDependency by instanceOf {
 *         SampleRepositoryWithDependency(inject(autoCloseRepository))
 *     }
 *     val interfaceRepo by instanceOf<SampleInterfaceRepo> { SampleInterfaceRepoImpl() }
 *     val constantValue by valueOf(42)
 *     val providable by providableOf { 34 }
 * }
 *
 * // ----- Usage -----
 *
 * // view model
 * class SomeVM(
 *     dep1: Dependency<SampleRepository> = Module.autoCloseRepository,
 * ) : ViewModel() {
 *     val dep1value = inject(dep1)
 *
 *     fun foo(){
 *         val dep2 = inject(Module.interfaceRepo)
 *     }
 * }
 *
 * // compose
 * @Composable
 * fun ComposeWithDI() {
 *     val repo1 = inject(Module.autoCloseRepository)
 *     val repo2 = inject { Module.repositoryWithParam }
 *     ...
 * }
 *
 * // random access
 * fun foo() {
 *     val scope = DIScope()
 *     val repo1 = Module.autoCloseRepository.injectedIn(scope)
 *     // update providable value
 *     (Module.providable as? ProvidableDependency<Int>)?.provides { 21 }
 *     ...
 *     scope.close()
 * }
 * ```
 */
public abstract class Leviathan {
    public companion object Companion {
        @JvmStatic
        protected operator fun <T> Dependency<T>.getValue(
            iRef: Leviathan,
            property: KProperty<*>
        ): Dependency<T> = this
    }

    /**
     * Defines a dependency as a value.
     * The value can be updated using the [ValueDependency.provides] method.
     * The value will be provided every time the dependency is injected.
     */
    protected fun <T> valueOf(
        value: T
    ): Dependency<T> =
        ValueDependency(value)

    /**
     * Defines a dependency as a providable function.
     * The provider function can be updated using [ProvidableDependency.provides] method.
     * The instance from the provider will be provided every time the dependency is injected.
     */
    protected fun <T> providableOf(
        provider: () -> T
    ): ProvidableDependency<T> =
        ProvidableDependency(provider)

    /**
     * Defines a dependency as a factory function.
     * If [useCache] is true (default), the same instance will be provided every time the dependency
     * is injected within the same [DIScope]. If false, a new instance will be created every time.
     */
    protected fun <T> factoryOf(
        useCache: Boolean = true,
        factory: DependencyInitializationScope.() -> T
    ): Dependency<T> =
        FactoryDependency(useCache, factory)

    /**
     * Defines a dependency as a singleton instance.
     * If [keepAlive] is true, the instance will be kept alive after being injected at least once.
     * If false (default), the instance will be destroyed as soon as the last [DIScope] using it is closed.
     */
    protected fun <T> instanceOf(
        keepAlive: Boolean = false,
        factory: DependencyInitializationScope.() -> T
    ): Dependency<T> =
        InstanceDependency(keepAlive, factory)
}
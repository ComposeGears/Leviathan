package com.composegears.leviathan

import kotlin.reflect.KProperty

// --- Marker -------------------------------------------

@DslMarker
public annotation class LeviathanDslMarker

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

@LeviathanDslMarker
public class DependencyInitializationScope internal constructor(
    private val diScope: DIScope
) {
    public fun <T> inject(dependency: Dependency<T>): T = dependency.injectedIn(diScope)
}

// ---  Dependency definition --------------------------------------

public interface Dependency<T> {
    public fun injectedIn(scope: DIScope): T

    public operator fun getValue(thisRef: Any?, property: KProperty<*>): Dependency<T> = this
}

public interface MutableDependency<T, P> : Dependency<T> {
    public fun provides(what: P)

    public override operator fun getValue(thisRef: Any?, property: KProperty<*>): MutableDependency<T, P> = this
}

/**
 * A dependency that provides a value using a provider function.
 * The provider can be updated using the [provides] method.
 */
internal class MutableDependencyImpl<T>(
    private var valueProvider: DependencyInitializationScope.() -> T
) : MutableDependency<T, DependencyInitializationScope.() -> T> {
    override fun injectedIn(scope: DIScope): T = valueProvider(
        DependencyInitializationScope(scope)
    )

    /**
     * Updates the provider to the given [what] function.
     */
    override fun provides(what: DependencyInitializationScope.() -> T) {
        valueProvider = what
    }
}

/**
 * A dependency that provides a new instance using a factory function.
 * If [cacheInScope] is true, the same instance will be provided every time the dependency
 * is injected within the same [DIScope]. If false, a new instance will be created every time.
 */
internal class FactoryDependencyImpl<T>(
    private val cacheInScope: Boolean,
    private val factory: DependencyInitializationScope.() -> T
) : Dependency<T> {
    private val scopedCache = mutableMapOf<DIScope, T>()
    override fun injectedIn(scope: DIScope): T =
        if (cacheInScope) {
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
 * A dependency that provides an instance using a factory function.
 * If [keepAlive] is true, the instance will be kept alive after being injected at least once.
 * If false, the instance will be destroyed as soon as the last [DIScope] using it is closed.
 */
internal class InstanceDependencyImpl<T>(
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

// ---  DSL definition ---------------------------------------------

/**
 * Marker interface and DSL receiver for Leviathan dependency declarations.
 *
 * This interface provides the receiver for extension builders:
 * [singleton], [factoryOf], [instanceOf], and [mutableOf].
 *
 * You can use the DSL in two equivalent styles:
 *
 * 1) Explicit receiver:
 *    `val repo by Leviathan.singleton { Repository() }`
 *
 * 2) Implicit receiver by implementing [Leviathan]:
 *    `class Module : Leviathan { val repo by singleton { Repository() } }`
 *
 * The companion object [Default] is exposed as `Leviathan` and serves as
 * the default shared receiver for DSL calls.
 */
public interface Leviathan {
    /**
     * Default shared DSL receiver, referenced in code as `Leviathan`.
     */
    public companion object Default : Leviathan
}

/**
 * Defines a dependency as a singleton within its declaration holder.
 *
 * The [valueProvider] is evaluated lazily on the first injection and its result is reused
 * for all subsequent injections from the same dependency instance, regardless of [DIScope].
 * The value is not cleared by scope closure; it stays alive as long as the dependency holder
 * instance itself stays alive (for example, a containing object/class instance).
 *
 * You can call `inject(...)` inside it to resolve other dependencies.
 *
 * Example:
 * `val repo by Leviathan.singleton { Repository(inject(apiClient)) }`
 *
 * @param valueProvider Factory block used to create the value once per dependency holder instance.
 * @return A [Dependency] that returns the same value for that holder instance.
 */
@Suppress("UnusedReceiverParameter")
public fun <T> Leviathan.singleton(
    valueProvider: DependencyInitializationScope.() -> T
): Dependency<T> =
    InstanceDependencyImpl(keepAlive = true) { valueProvider() }

/**
 * Defines a dependency as a factory.
 *
 * The [factory] block is executed to produce values on injection.
 *
 * - When [cacheInScope] is `true` (default), one value is created per [DIScope] and
 *   reused for subsequent injections in that same scope.
 * - When [cacheInScope] is `false`, a new value is created on every injection call.
 *
 * Cached values are released when their corresponding [DIScope] is closed.
 *
 * You can call `inject(...)` inside it to resolve other dependencies.
 *
 * Example:
 * `val repo by Leviathan.factoryOf { Repository(inject(api)) }`
 *
 * @param cacheInScope Controls whether values are cached per scope.
 * @param factory Factory block that creates the value.
 * @return A [Dependency] with per-scope cached or always-new behavior.
 */
@Suppress("UnusedReceiverParameter")
public fun <T> Leviathan.factoryOf(
    cacheInScope: Boolean = true,
    factory: DependencyInitializationScope.() -> T
): Dependency<T> =
    FactoryDependencyImpl(cacheInScope, factory)

/**
 * Defines a dependency as a scoped shared instance.
 *
 * A single instance is created lazily on first injection and then shared across all active scopes
 * that use this dependency. It is one shared instance while at least one participating scope remains active.
 *
 * Lifecycle behavior:
 * - The instance is created on first injection.
 * - Each injecting [DIScope] is tracked.
 * - When a tracked scope is closed, it is removed from tracking.
 * - When the last tracked scope is closed, the instance is cleared.
 * - The next injection creates a new instance.
 *
 * You can call `inject(...)` inside it to resolve other dependencies.
 *
 * Example:
 * `val session by Leviathan.instanceOf { UserSession(inject(repository)) }`
 *
 * @param factory Factory block that creates the shared instance.
 * @return A [Dependency] whose instance lives while at least one using scope is alive.
 */
@Suppress("UnusedReceiverParameter")
public fun <T> Leviathan.instanceOf(
    factory: DependencyInitializationScope.() -> T
): Dependency<T> =
    InstanceDependencyImpl(keepAlive = false, factory)

/**
 * Defines a dependency as a mutable provider.
 *
 * The current [valueProvider] function is invoked on each injection.
 * You can replace the provider later via [MutableDependency.provides], which changes
 * how future injections are resolved.
 *
 * This is useful for runtime reconfiguration, test overrides, and lightweight swapping
 * of produced values without recreating the dependency declaration.
 *
 * You can call `inject(...)` inside it to resolve other dependencies.
 *
 * Example:
 * `val endpoint by Leviathan.mutableOf { ServiceImpl() }`
 *
 * @param valueProvider Initial provider function used to resolve values.
 * @return A [MutableDependency] whose provider can be updated at runtime.
 */
@Suppress("UnusedReceiverParameter")
public fun <T> Leviathan.mutableOf(
    valueProvider: DependencyInitializationScope.() -> T
): MutableDependency<T, DependencyInitializationScope.() -> T> =
    MutableDependencyImpl(valueProvider)
package com.composegears.leviathan

import kotlin.reflect.KProperty

/**
 * Annotation to mark delicate APIs in Leviathan.
 */
@Retention(AnnotationRetention.BINARY)
@RequiresOptIn(level = RequiresOptIn.Level.WARNING, message = "Developed for testing purposes")
public annotation class LeviathanDelicateApi

/**
 * Base service locator class.
 */
public abstract class Leviathan {
    public companion object;

    /**
     * Creates a factory-based dependency provider.
     *
     * @param provider dependency provider.
     * @return dependency provider instance.
     */
    protected fun <T> factoryOf(provider: () -> T): DependencyProvider<Dependency<T>> =
        DependencyProvider(Factory(provider))

    /**
     * Creates a lazy instance-based dependency provider.
     *
     * @param provider dependency provider.
     * @return dependency provider instance.
     */
    protected fun <T> instanceOf(provider: () -> T): DependencyProvider<Dependency<T>> =
        instanceOf(true, provider)

    /**
     * Creates an instance-based dependency provider.
     *
     * @param lazy whether the instance should be lazily initialized.
     * @param provider dependency provider.
     * @return dependency provider instance.
     */
    protected fun <T> instanceOf(lazy: Boolean, provider: () -> T): DependencyProvider<Dependency<T>> =
        if (lazy) DependencyProvider(Instance(provider))
        else provider().let { factoryOf { it } }

    /**
     * Creates an instance-based late-init dependency provider.
     *
     * Call [LateInitDependency.provides] to set the provider.
     *
     * @return dependency provider instance.
     */
    protected fun <T> lateInitInstance(): DependencyProvider<LateInitDependency<T>> =
        DependencyProvider(LateInitInstance())

    protected operator fun <P, T : Dependency<P>> DependencyProvider<T>.getValue(
        di: Any?,
        property: KProperty<*>
    ): T = dependency
}

// ---------------------------------API---------------------------------

/**
 * Abstract class representing a dependency.
 *
 * @param T the type of the dependency.
 */
public abstract class Dependency<T> internal constructor() {
    /**
     * @return the dependency instance.
     */
    public abstract fun get(): T

    public operator fun getValue(thisRef: Any?, property: KProperty<*>): T = get()

    /**
     * Overrides the current dependency.
     *
     * @param provider new dependency provider
     */
    @LeviathanDelicateApi
    public abstract fun overrideWith(provider: (() -> T)?)
}

/**
 * Class representing a late-initialized dependency.
 *
 * @param T The type of the dependency.
 */
public abstract class LateInitDependency<T> internal constructor() : Dependency<T>() {
    /**
     * Sets the provider for the dependency.
     *
     * @param provider dependency provider.
     */
    public abstract fun provides(provider: () -> T)
}

/**
 * Dependency provider delegate
 *
 * @param T The type of the dependency.
 * @property dependency The dependency instance.
 */
@Suppress("UseDataClass")
public class DependencyProvider<T : Dependency<*>> internal constructor(
    internal val dependency: T
)

// ---------------------------------Implementation---------------------------------

internal class Instance<T>(provider: () -> T) : Dependency<T>() {
    private var oValue: T? = null
    private val dependency by lazy(provider)

    override fun get(): T = oValue ?: dependency

    @OptIn(LeviathanDelicateApi::class)
    override fun overrideWith(provider: (() -> T)?) {
        oValue = provider?.invoke()
    }
}

internal class LateInitInstance<T> : LateInitDependency<T>() {
    private var provider: (() -> T)? = null
    private var oValueProvider: (() -> T)? = null

    override fun provides(provider: () -> T) {
        this.provider = provider
    }

    override fun get(): T =
        if (oValueProvider != null) oValueProvider!!()
        else provider?.invoke() ?: error("LateInitInstance is not initialized")

    @LeviathanDelicateApi
    override fun overrideWith(provider: (() -> T)?) {
        oValueProvider = provider
    }
}

internal class Factory<T>(private val provider: () -> T) : Dependency<T>() {
    private var oValueProvider: (() -> T)? = null

    override fun get(): T =
        if (oValueProvider != null) oValueProvider!!()
        else provider()

    @OptIn(LeviathanDelicateApi::class)
    override fun overrideWith(provider: (() -> T)?) {
        oValueProvider = provider
    }
}
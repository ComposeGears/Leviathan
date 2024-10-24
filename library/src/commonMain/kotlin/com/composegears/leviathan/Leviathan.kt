package com.composegears.leviathan

import kotlin.reflect.KProperty

@Retention(AnnotationRetention.BINARY)
@RequiresOptIn(level = RequiresOptIn.Level.WARNING, message = "Developed for testing purposes")
public annotation class LeviathanDelicateApi

/**
 * Service locator di base
 */
public abstract class Leviathan {
    public companion object;

    protected fun <T> factoryOf(provider: () -> T): DependencyProvider<T> =
        DependencyProvider(Factory(provider))

    protected fun <T> instanceOf(lazy: Boolean = true, provider: () -> T): DependencyProvider<T> =
        if (lazy) DependencyProvider(Instance(provider))
        else provider().let { factoryOf { it } }

    protected operator fun <T> DependencyProvider<T>.getValue(
        di: Any?, property: KProperty<*>
    ): Dependency<T> = dependency
}

public abstract class Dependency<T> internal constructor() {
    public abstract fun get(): T

    public operator fun getValue(thisRef: Any?, property: KProperty<*>): T = get()

    @LeviathanDelicateApi
    public abstract fun overrideWith(provider: (() -> T)?)
}

public class DependencyProvider<T> internal constructor(
    internal val dependency: Dependency<T>
)

internal class Instance<T>(provider: () -> T) : Dependency<T>() {
    private var oValue: T? = null
    private val dependency by lazy(provider)

    override fun get(): T = oValue ?: dependency

    @OptIn(LeviathanDelicateApi::class)
    override fun overrideWith(provider: (() -> T)?) {
        oValue = provider?.invoke()
    }
}

internal class Factory<T>(private val provider: () -> T) : Dependency<T>() {
    private var oValueProvider: (() -> T)? = null

    override fun get(): T = if (oValueProvider != null) oValueProvider!!() else provider()

    @OptIn(LeviathanDelicateApi::class)
    override fun overrideWith(provider: (() -> T)?) {
        oValueProvider = provider
    }
}
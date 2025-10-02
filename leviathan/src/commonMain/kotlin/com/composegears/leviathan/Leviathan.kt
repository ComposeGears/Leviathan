package com.composegears.leviathan

import kotlin.jvm.JvmStatic
import kotlin.reflect.KProperty

// ---  Scope definition -------------------------------------------

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

// public, useful for testing
public class ValueDependency<T>(
    private val value: T
) : Dependency<T> {
    override fun injectedIn(scope: DIScope): T = value
}

internal class FactoryDependency<T>(
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

internal class InstanceDependency<T>(
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

public abstract class Leviathan {
    public companion object Companion {
        @JvmStatic
        protected operator fun <T> Dependency<T>.getValue(
            iRef: Leviathan,
            property: KProperty<*>
        ): Dependency<T> = this
    }

    protected fun <T> valueOf(
        value: T
    ): Dependency<T> =
        ValueDependency(value)

    protected fun <T> factoryOf(
        useCache: Boolean = true,
        factory: DependencyInitializationScope.() -> T
    ): Dependency<T> =
        FactoryDependency(useCache, factory)

    protected fun <T> instanceOf(
        keepAlive: Boolean = false,
        factory: DependencyInitializationScope.() -> T
    ): Dependency<T> =
        InstanceDependency(keepAlive, factory)
}
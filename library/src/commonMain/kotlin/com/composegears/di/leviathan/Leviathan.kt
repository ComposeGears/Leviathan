package com.composegears.di.leviathan

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Service locator di base
 */
abstract class Leviathan {
    companion object;

    //----- Providers -----

    /**
     * Provide same instance of service for all requests (lazy initialization)
     *
     * @param factory factory method to create service objects
     */
    protected fun <T> instance(factory: () -> T): ReadOnlyProperty<Leviathan, T> = instance(true, factory)

    /**
     * Provide same instance of service for all requests
     *
     * @param lazy true to create service upon first call, false to create upon declaration
     * @param factory factory method to create service objects
     */
    protected fun <T> instance(lazy: Boolean, factory: () -> T): ReadOnlyProperty<Leviathan, T> {
        return if (lazy) {
            LazyServiceDelegate(factory)
        } else {
            val service = factory()
            ProvidableServiceDelegate { service }
        }
    }

    /**
     * Provide new service on each access
     *
     * @param factory factory method to create service
     */
    protected fun <T> factory(factory: () -> T): ReadOnlyProperty<Leviathan, T> {
        return ProvidableServiceDelegate { factory() }
    }

    //----- Helpers -----
    /**
     * Lazy service implementation
     */
    class LazyServiceDelegate<T>(provider: () -> T) : ReadOnlyProperty<Leviathan, T> {
        private val service by lazy(provider)
        override fun getValue(thisRef: Leviathan, property: KProperty<*>): T = service
    }

    /**
     * Factory service implementation
     */
    class ProvidableServiceDelegate<T>(val provider: () -> T) : ReadOnlyProperty<Leviathan, T> {
        override fun getValue(thisRef: Leviathan, property: KProperty<*>): T = provider()
    }
}
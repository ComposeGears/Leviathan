package com.composegears.leviathan.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.RetainObserver
import androidx.compose.runtime.retain.retain
import androidx.lifecycle.ViewModel
import com.composegears.leviathan.DIScope
import com.composegears.leviathan.Dependency

// ---------- ViewModel integration --------------------------------

private class ViewModelDIScope : AutoCloseable {
    companion object {
        const val KEY = "VM-LEVIATHAN-DI-SCOPE"
    }

    val diScope = DIScope()

    override fun close() {
        diScope.close()
    }
}

/**
 * Injects a dependency within a ViewModel scope.
 * The scope is tied to the ViewModel's lifecycle.
 *
 * @param dependency The dependency to inject.
 * @return The injected dependency instance.
 */
public fun <T> ViewModel.inject(dependency: Dependency<T>): T {
    var scopeHolder = getCloseable<ViewModelDIScope>(ViewModelDIScope.KEY)
    if (scopeHolder == null) {
        scopeHolder = ViewModelDIScope()
        addCloseable(ViewModelDIScope.KEY, scopeHolder)
    }
    return dependency.injectedIn(scopeHolder.diScope)
}

// ---------- Compose integration ----------------------------------

public enum class ComposeInjectionRetention {
    Remember,
    Retain,
}

public class ComposeDIScope : DIScope(), RememberObserver, RetainObserver {
    override fun onRemembered(): Unit = Unit
    override fun onForgotten(): Unit = close()
    override fun onAbandoned(): Unit = Unit
    override fun onRetained(): Unit = Unit
    override fun onEnteredComposition(): Unit = Unit
    override fun onExitedComposition(): Unit = Unit
    override fun onRetired(): Unit = close()
    override fun onUnused(): Unit = Unit
}

/**
 * Injects a dependency within a Compose scope.
 * The dependency will be remembered.
 *
 * @param dependency The dependency to inject.
 * @return The injected dependency instance.
 */
@Composable
public fun <T> inject(dependency: Dependency<T>): T {
    val scope = remember { ComposeDIScope() }
    return remember { dependency.injectedIn(scope) }
}

/**
 * Injects a dependency within a Compose scope.
 * The dependency will be remembered.
 *
 * @param dependency A lambda that provides the dependency to inject.
 * @return The injected dependency instance.
 */
@Composable
public fun <T> inject(dependency: () -> Dependency<T>): T = inject(dependency())

/**
 * Injects a dependency within a Compose scope.
 * The dependency will be retained.
 *
 * @param dependency The dependency to inject.
 * @return The injected dependency instance.
 */
@Composable
public inline fun <reified T> injectAndRetain(dependency: Dependency<T>): T {
    val scope = retain { ComposeDIScope() }
    return retain { dependency.injectedIn(scope) }
}

/**
 * Injects a dependency within a Compose scope.
 * The dependency will be retained.
 *
 * @param dependency A lambda that provides the dependency to inject.
 * @return The injected dependency instance.
 */
@Composable
public inline fun <reified T> injectAndRetain(dependency: () -> Dependency<T>): T = injectAndRetain(dependency())
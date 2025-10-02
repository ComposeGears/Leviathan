package com.composegears.leviathan.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
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

public fun <T> ViewModel.inject(dependency: Dependency<T>): T {
    var scopeHolder = getCloseable<ViewModelDIScope>(ViewModelDIScope.KEY)
    if (scopeHolder == null) {
        scopeHolder = ViewModelDIScope()
        addCloseable(ViewModelDIScope.KEY, scopeHolder)
    }
    return dependency.injectedIn(scopeHolder.diScope)
}

// ---------- Compose integration ----------------------------------

@Composable
public fun <T> inject(dependency: Dependency<T>): T {
    val scope = remember { DIScope() }
    DisposableEffect(scope) {
        onDispose {
            scope.close()
        }
    }
    return remember { dependency.injectedIn(scope) }
}

@Composable
public fun <T> inject(dependency: () -> Dependency<T>): T = inject(dependency())
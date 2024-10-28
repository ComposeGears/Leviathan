package com.composegears.leviathan.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.composegears.leviathan.Dependency

@Composable
public fun <T> leviathanInject(dependency: () -> Dependency<T>): T = remember { dependency().get() }
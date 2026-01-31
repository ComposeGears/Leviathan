package com.composegears.leviathan.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.runtime.retain.LocalRetainedValuesStore
import androidx.compose.runtime.retain.ManagedRetainedValuesStore
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import com.composegears.leviathan.Leviathan
import kotlin.test.*

class LeviathanComposeTest {

    class Service

    class TestDI : Leviathan() {
        val service by instanceOf(keepAlive = false) { Service() }
    }

    class TestVN(testDI: TestDI) : ViewModel() {
        val service1: Service = inject(testDI.service)
        val service2: Service = inject(testDI.service)
    }

    @Test
    fun `ViewModel - inject provides same instance within ViewModel scope`() {
        val testDI = TestDI()
        val vm = TestVN(testDI)
        assertEquals(vm.service1, vm.service2)
    }

    @Test
    fun `ViewModel - inject provides different instances after ViewModel reinitialization`() {
        val testDI = TestDI()
        val vmStore = ViewModelStore()
        val vm1 = TestVN(testDI)
        vmStore.put("vm", vm1)
        val service = vm1.service1
        vmStore.clear()
        val vm2 = TestVN(testDI)
        assertNotEquals(service, vm2.service1)
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun `Compose - inject provides same instance within composition scope`() = runComposeUiTest {
        val testDI = TestDI()
        setContent {
            val service1 = inject(testDI.service)
            val service2 = inject(testDI.service)
            assertEquals(service1, service2)
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun `Compose - inject provides new instance after recomposition`() = runComposeUiTest {
        val testDI = TestDI()
        var outerService1: Service? = null
        var outerService2: Service? = null

        @Composable
        fun OuterComposable1() {
            val service = inject(testDI.service)
            LaunchedEffect(Unit) {
                outerService1 = service
            }
        }

        @Composable
        fun OuterComposable2() {
            val service = inject(testDI.service)
            LaunchedEffect(Unit) {
                outerService2 = service
            }
        }
        setContent {
            var switch by remember { mutableStateOf(0) }
            when (switch) {
                0 -> OuterComposable1()
                1 -> Unit
                2 -> OuterComposable2()
                else -> Unit
            }
            Box(Modifier.testTag("switch").size(8.dp).clickable { switch++ })
        }
        awaitIdle()
        onNodeWithTag("switch").performClick()
        awaitIdle()
        onNodeWithTag("switch").performClick()
        awaitIdle()
        assertNotNull(outerService1, "OuterService1 is null")
        assertNotNull(outerService2, "OuterService2 is null")
        assertNotEquals(outerService1, outerService2, "Services are equal")
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun `Compose - retained injection persist across recomposition`() = runComposeUiTest {
        val testDI = TestDI()
        var retainedService: Service? = null
        var retainChecks = 0
        val retainStore = ManagedRetainedValuesStore()
        retainStore.enableRetainingExitedValues()

        @Composable
        fun RetainCall() {
            val service = injectAndRetain(testDI.service)
            LaunchedEffect(Unit) {
                println("Retained service instance: $service --> $retainedService")
                if (retainedService == null) retainedService = service
                else {
                    retainChecks++
                    assertEquals(retainedService, service)
                }
            }
        }
        setContent {
            CompositionLocalProvider(
                LocalRetainedValuesStore provides retainStore
            ) {
                var switch by remember { mutableStateOf(true) }
                if (switch) RetainCall() else Unit
                Box(Modifier.testTag("switch").size(8.dp).clickable { switch = !switch })
            }
        }
        awaitIdle()
        onNodeWithTag("switch").performClick()
        awaitIdle()
        onNodeWithTag("switch").performClick()
        awaitIdle()
        onNodeWithTag("switch").performClick()
        awaitIdle()
        assertTrue(retainChecks >= 1)
        assertNotNull(retainedService)
    }
}
package com.composegears.leviathan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

//Simple providable service
open class Service(initAction: (() -> Unit)? = null) {
    init {
        initAction?.invoke()
    }
}

//Service based on another service
class DependService(val s: Service) : Service()

class CyclicService(val sp: () -> Service) : Service()

//Outer independent ServiceLocator
class ExternalServices : Leviathan() {
    val service by instanceOf(false) { Service() }
}

//Main ServiceLocator
class ServiceLocator(externalServices: ExternalServices) : Leviathan() {
    // instances
    val autoCloseInstance by instanceOf(false) { Service() }
    val keepAliveInstance by instanceOf(true) { Service() }

    // factories
    val alwaysNewFactory by factoryOf(false) { Service() }
    val cachedFactory by factoryOf(true) { Service() }

    // external
    val external by externalServices.service

    // dependency
    val subInstance by instanceOf { Service() }
    val dependInstance by instanceOf { DependService(inject(subInstance)) }

    // cyclic
    val cyclicDep1: Dependency<CyclicService> by instanceOf { CyclicService { inject(cyclicDep2) } }
    val cyclicDep2: Dependency<CyclicService> by instanceOf { CyclicService { inject(cyclicDep1) } }

    // value
    val valueDep by valueOf(Service())

    // providable
    val mutableValueDep by mutableValueOf(Service())
    val providableDep by providableOf { Service() }
}

//------------Code------------

class Tests {

    // DIScope tests

    @Test
    fun `DIScope - executes close actions on close`() {
        val scope = DIScope()
        var executed = false
        scope.onClose { executed = true }
        scope.close()
        assertEquals(true, executed, "Close action should be executed")
    }

    @Test
    fun `DIScope - executes multiple close actions in order`() {
        val scope = DIScope()
        val executionOrder = mutableListOf<Int>()
        scope.onClose { executionOrder.add(1) }
        scope.onClose { executionOrder.add(2) }
        scope.onClose { executionOrder.add(3) }
        scope.close()
        assertEquals(listOf(1, 2, 3), executionOrder, "Close actions should execute in order")
    }

    @Test
    fun `DIScope - clears close actions after close`() {
        val scope = DIScope()
        var count = 0
        scope.onClose { count++ }
        scope.close()
        scope.close() // Second close should not execute actions again
        assertEquals(1, count, "Close actions should only execute once")
    }

    @Test
    fun `DIScope-GLOBAL - does not execute close actions`() {
        var executed = false
        DIScope.GLOBAL.onClose { executed = true }
        DIScope.GLOBAL.close()
        assertEquals(false, executed, "Global scope should not execute close actions")
    }

    // FactoryDependency tests with useCache=true
    @Test
    fun `cachedFactory - caches instances within same scope`() {
        val externalServices = ExternalServices()
        val serviceLocator = ServiceLocator(externalServices)
        val scope = DIScope()

        val instance1 = serviceLocator.cachedFactory.injectedIn(scope)
        val instance2 = serviceLocator.cachedFactory.injectedIn(scope)

        assertEquals(instance1, instance2, "Cached factory should return same instance within scope")
    }

    @Test
    fun `cachedFactory - creates new instances in different scopes`() {
        val externalServices = ExternalServices()
        val serviceLocator = ServiceLocator(externalServices)
        val scope1 = DIScope()
        val scope2 = DIScope()

        val instance1 = serviceLocator.cachedFactory.injectedIn(scope1)
        val instance2 = serviceLocator.cachedFactory.injectedIn(scope2)

        assertNotEquals(instance1, instance2, "Cached factory should create new instance in different scope")
    }

    // FactoryDependency tests with useCache=false
    @Test
    fun `alwaysNewFactory - creates new instances on each call`() {
        val externalServices = ExternalServices()
        val serviceLocator = ServiceLocator(externalServices)
        val scope = DIScope()

        val instance1 = serviceLocator.alwaysNewFactory.injectedIn(scope)
        val instance2 = serviceLocator.alwaysNewFactory.injectedIn(scope)
        val instance3 = serviceLocator.alwaysNewFactory.injectedIn(scope)

        assertNotEquals(instance1, instance2, "Should create new instance on each call")
        assertNotEquals(instance2, instance3, "Should create new instance on each call")
        assertNotEquals(instance1, instance3, "Should create new instance on each call")
    }

    // InstanceDependency tests with keepAlive=false (auto-close)
    @Test
    fun `autoCloseInstance - reuses instance within same scope`() {
        val externalServices = ExternalServices()
        val serviceLocator = ServiceLocator(externalServices)
        val scope = DIScope()

        val instance1 = serviceLocator.autoCloseInstance.injectedIn(scope)
        val instance2 = serviceLocator.autoCloseInstance.injectedIn(scope)

        assertEquals(instance1, instance2, "Auto-close instance should reuse instance within scope")
    }

    @Test
    fun `autoCloseInstance - creates same instances in different scopes`() {
        val externalServices = ExternalServices()
        val serviceLocator = ServiceLocator(externalServices)
        val scope1 = DIScope()
        val scope2 = DIScope()

        val instance1 = serviceLocator.autoCloseInstance.injectedIn(scope1)
        val instance2 = serviceLocator.autoCloseInstance.injectedIn(scope2)

        assertEquals(instance1, instance2, "Auto-close instance should reuse instances in different scopes")
    }

    @Test
    fun `autoCloseInstance - nullifies instance when all scopes close`() {
        val externalServices = ExternalServices()
        val serviceLocator = ServiceLocator(externalServices)
        val scope1 = DIScope()
        val scope2 = DIScope()

        val instance1 = serviceLocator.autoCloseInstance.injectedIn(scope1)
        val instance2 = serviceLocator.autoCloseInstance.injectedIn(scope2)
        assertEquals(instance1, instance2, "Should be same instance across scopes")

        scope1.close()
        val instance3 = serviceLocator.autoCloseInstance.injectedIn(scope2)
        assertEquals(instance1, instance3, "Should still be same instance while one scope active")

        scope2.close()
        val newScope = DIScope()
        val instance4 = serviceLocator.autoCloseInstance.injectedIn(newScope)
        assertNotEquals(instance1, instance4, "Should create new instance after all scopes close")
    }

    // InstanceDependency tests with keepAlive=true
    @Test
    fun `keepAliveInstance - persists across different scopes`() {
        val externalServices = ExternalServices()
        val serviceLocator = ServiceLocator(externalServices)
        val scope1 = DIScope()
        val scope2 = DIScope()

        val instance1 = serviceLocator.keepAliveInstance.injectedIn(scope1)
        val instance2 = serviceLocator.keepAliveInstance.injectedIn(scope2)

        assertEquals(instance1, instance2, "Keep-alive instance should persist across scopes")
    }

    @Test
    fun `keepAliveInstance - survives scope closure`() {
        val externalServices = ExternalServices()
        val serviceLocator = ServiceLocator(externalServices)
        val scope1 = DIScope()

        val instance1 = serviceLocator.keepAliveInstance.injectedIn(scope1)
        scope1.close()

        val scope2 = DIScope()
        val instance2 = serviceLocator.keepAliveInstance.injectedIn(scope2)

        assertEquals(instance1, instance2, "Keep-alive instance should survive scope closure")
    }

    // DependencyInitializationScope tests
    @Test
    fun `inject - resolves dependencies during initialization`() {
        val externalServices = ExternalServices()
        val serviceLocator = ServiceLocator(externalServices)
        val scope = DIScope()

        val dependInstance = serviceLocator.dependInstance.injectedIn(scope)
        val subInstance = serviceLocator.subInstance.injectedIn(scope)

        assertEquals(subInstance, dependInstance.s, "Inject should resolve correct dependency")
    }

    @Test
    fun `inject - works with nested dependencies`() {
        val scope = DIScope()

        // Create a more complex dependency chain
        val testLocator = object : Leviathan() {
            val level1 by instanceOf { Service() }
            val level2 by instanceOf { DependService(inject(level1)) }
            val level3 by instanceOf { DependService(inject(level2)) }
        }

        val level3Instance = testLocator.level3.injectedIn(scope)
        val level2Instance = testLocator.level2.injectedIn(scope)
        val level1Instance = testLocator.level1.injectedIn(scope)

        assertEquals(level2Instance, level3Instance.s, "Level 3 should depend on level 2")
        assertEquals(level1Instance, level2Instance.s, "Level 2 should depend on level 1")
    }

    // Cyclic dependency tests
    @Test
    fun `cyclicDep1 - resolves cyclic dependencies correctly`() {
        val externalServices = ExternalServices()
        val serviceLocator = ServiceLocator(externalServices)
        val scope = DIScope()

        val cyclicDep1 = serviceLocator.cyclicDep1.injectedIn(scope)
        val cyclicDep2 = serviceLocator.cyclicDep2.injectedIn(scope)

        val dep2FromDep1 = cyclicDep1.sp()
        val dep1FromDep2 = cyclicDep2.sp()

        assertEquals(cyclicDep2, dep2FromDep1, "Cyclic dep1 should reference dep2")
        assertEquals(cyclicDep1, dep1FromDep2, "Cyclic dep2 should reference dep1")
    }

    // External dependency tests
    @Test
    fun `external - accesses external service dependency`() {
        val externalServices = ExternalServices()
        val serviceLocator = ServiceLocator(externalServices)
        val scope = DIScope()

        val externalInstance = serviceLocator.external.injectedIn(scope)
        val directExternal = externalServices.service.injectedIn(scope)

        assertEquals(directExternal, externalInstance, "External dependency should reference same instance")
    }

    @Test
    fun `external - maintains independence from main locator`() {
        val externalServices1 = ExternalServices()
        val externalServices2 = ExternalServices()
        val serviceLocator1 = ServiceLocator(externalServices1)
        val serviceLocator2 = ServiceLocator(externalServices2)
        val scope = DIScope()

        val external1 = serviceLocator1.external.injectedIn(scope)
        val external2 = serviceLocator2.external.injectedIn(scope)

        assertNotEquals(external1, external2, "External dependencies should be independent")
    }

    // ValueDependency tests
    @Test
    fun `valueDep - provides consistent value instance`() {
        val externalServices = ExternalServices()
        val serviceLocator = ServiceLocator(externalServices)
        val scope1 = DIScope()
        val scope2 = DIScope()
        val value1 = serviceLocator.valueDep.injectedIn(scope1)
        val value2 = serviceLocator.valueDep.injectedIn(scope1)
        val value3 = serviceLocator.valueDep.injectedIn(scope2)
        assertEquals(value1, value2, "Value dependency should provide consistent instance")
        assertEquals(value2, value3, "Value dependency should provide consistent instance")
    }

    @Test
    fun `mutableValueDep - reflect changes to provider`() {
        val externalServices = ExternalServices()
        val serviceLocator = ServiceLocator(externalServices)
        val scope = DIScope()
        val providedInstance = Service()
        serviceLocator.mutableValueDep.provides(providedInstance)
        val instance = serviceLocator.mutableValueDep.injectedIn(scope)
        assertEquals(providedInstance, instance, "MutableValue dependency should return provided instance")
    }

    // ProvidableDependency tests

    @Test
    fun `providableDep - reflect changes to provider`() {
        val externalServices = ExternalServices()
        val serviceLocator = ServiceLocator(externalServices)
        val scope = DIScope()
        val providedInstance = Service()
        serviceLocator.providableDep.provides { providedInstance }
        val instance = serviceLocator.providableDep.injectedIn(scope)
        assertEquals(providedInstance, instance, "Providable dependency should return provided instance")
    }

    // Scope behavior combination tests
    @Test
    fun `mixed dependencies - behave correctly in same scope`() {
        val externalServices = ExternalServices()
        val serviceLocator = ServiceLocator(externalServices)
        val scope = DIScope()

        val autoClose1 = serviceLocator.autoCloseInstance.injectedIn(scope)
        val autoClose2 = serviceLocator.autoCloseInstance.injectedIn(scope)
        val keepAlive1 = serviceLocator.keepAliveInstance.injectedIn(scope)
        val keepAlive2 = serviceLocator.keepAliveInstance.injectedIn(scope)
        val cached1 = serviceLocator.cachedFactory.injectedIn(scope)
        val cached2 = serviceLocator.cachedFactory.injectedIn(scope)
        val alwaysNew1 = serviceLocator.alwaysNewFactory.injectedIn(scope)
        val alwaysNew2 = serviceLocator.alwaysNewFactory.injectedIn(scope)

        assertEquals(autoClose1, autoClose2, "Auto-close should be same within scope")
        assertEquals(keepAlive1, keepAlive2, "Keep-alive should be same within scope")
        assertEquals(cached1, cached2, "Cached should be same within scope")
        assertNotEquals(alwaysNew1, alwaysNew2, "Always-new should be different within scope")
    }

    @Test
    fun `mixed dependencies - behave correctly across scopes`() {
        val externalServices = ExternalServices()
        val serviceLocator = ServiceLocator(externalServices)
        val scope1 = DIScope()
        val scope2 = DIScope()

        val autoClose1 = serviceLocator.autoCloseInstance.injectedIn(scope1)
        val autoClose2 = serviceLocator.autoCloseInstance.injectedIn(scope2)
        val keepAlive1 = serviceLocator.keepAliveInstance.injectedIn(scope1)
        val keepAlive2 = serviceLocator.keepAliveInstance.injectedIn(scope2)
        val cached1 = serviceLocator.cachedFactory.injectedIn(scope1)
        val cached2 = serviceLocator.cachedFactory.injectedIn(scope2)

        assertEquals(autoClose1, autoClose2, "Auto-close should be same across scopes")
        assertEquals(keepAlive1, keepAlive2, "Keep-alive should be same across scopes")
        assertNotEquals(cached1, cached2, "Cached should be different across scopes")
    }
}
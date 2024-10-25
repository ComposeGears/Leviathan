package com.composegears.leviathan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
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
    val instance by instanceOf { Service() }
    val nonLazyInstance by instanceOf(false) { Service() }
    val dependInstance by instanceOf { DependService(instance.get()) }
    val factory by factoryOf { Service() }
    val delegatedInstance = externalServices.service
    val lateInitInstance by lateInitInstance<Service>()
    val cyclicDep1: Dependency<CyclicService> by instanceOf { CyclicService { cyclicDep2.get() } }
    val cyclicDep2: Dependency<CyclicService> by instanceOf { CyclicService { cyclicDep1.get() } }
}

//------------Code------------

class Tests {
    private val esl = ExternalServices()

    @Test
    fun instance_provide_same_objects() {
        val sl = ServiceLocator(esl)
        val instance = sl.instance
        assertEquals(sl.instance.get(), sl.instance.get())
        assertEquals(instance, instance)
    }

    @Test
    fun factory_provide_new_objects_on_every_access() {
        val sl = ServiceLocator(esl)
        assertNotEquals(sl.factory.get(), sl.factory.get())
    }

    @Test
    fun dependInstance_use_same_object_as_used_instance() {
        val sl = ServiceLocator(esl)
        val s = sl.instance
        val dps = sl.dependInstance
        assertEquals(dps.get().s, s.get())
        assertEquals(sl.dependInstance.get().s, sl.instance.get())
    }

    @Test
    fun delegatedInstance_provide_same_object_and_original_service() {
        val sl = ServiceLocator(esl)
        val ess = esl.service
        val dps = sl.delegatedInstance
        assertEquals(ess.get(), dps.get())
        assertEquals(sl.delegatedInstance.get(), esl.service.get())
    }

    @Test
    fun lateInitInstance_throw_exception_when_not_provided() {
        val sl = ServiceLocator(esl)
        assertFailsWith<IllegalStateException> { sl.lateInitInstance.get() }
    }

    @Test
    fun lateInitInstance_provide_provided_instance() {
        val sl = ServiceLocator(esl)
        val s = Service()
        sl.lateInitInstance.provides { s }
        assertEquals(sl.lateInitInstance.get(), s)
    }

    @Test
    fun cyclicService_cyclic_services_provide_appropriate_dependencies() {
        val sl = ServiceLocator(esl)
        val c1 = sl.cyclicDep1
        val c2 = sl.cyclicDep2
        assertEquals(sl.cyclicDep1.get().sp(), sl.cyclicDep2.get())
        assertEquals(sl.cyclicDep2.get().sp(), sl.cyclicDep1.get())
        assertEquals(c1.get().sp(), c2.get())
        assertEquals(c2.get().sp(), c1.get())
    }

    @Test
    fun global_provides_appropriate_instances() {
        val sl = ServiceLocator(esl)
        assertEquals(sl.instance.get(), sl.instance.get())
        assertEquals(sl.nonLazyInstance.get(), sl.nonLazyInstance.get())
        assertEquals(sl.dependInstance.get(), sl.dependInstance.get())
        assertEquals(sl.delegatedInstance.get(), sl.delegatedInstance.get())
        assertNotEquals(sl.factory.get(), sl.factory.get())
    }
}
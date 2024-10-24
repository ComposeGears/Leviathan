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
    val service by instance(false) { Service() }
}

//Main ServiceLocator
class ServiceLocator(externalServices: ExternalServices) : Leviathan() {
    val instance by instance { Service() }
    val nonLazyInstance by instance(false) { Service() }
    val dependInstance by instance { DependService(instance) }
    val factory by factory { Service() }
    val delegatedInstance = externalServices.service
    val cyclicDep1: CyclicService by instance { CyclicService { cyclicDep2 } }
    val cyclicDep2: CyclicService by instance { CyclicService { cyclicDep1 } }
}

//------------Code------------

class Tests {
    private val esl = ExternalServices()

    @Test
    fun instance_provide_same_objects() {
        val sl = ServiceLocator(esl)
        val instance = sl.instance
        assertEquals(sl.instance, sl.instance)
        assertEquals(instance, instance)
    }

    @Test
    fun factory_provide_new_objects_on_every_access() {
        val sl = ServiceLocator(esl)
        assertNotEquals(sl.factory, sl.factory)
    }

    @Test
    fun dependInstance_use_same_object_as_used_instance() {
        val sl = ServiceLocator(esl)
        val s = sl.instance
        val dps = sl.dependInstance
        assertEquals(dps.s, s)
        assertEquals(sl.dependInstance.s, sl.instance)
    }

    @Test
    fun delegatedInstance_provide_same_object_and_original_service() {
        val sl = ServiceLocator(esl)
        val ess = esl.service
        val dps = sl.delegatedInstance
        assertEquals(ess, dps)
        assertEquals(sl.delegatedInstance, esl.service)
    }


    @Test
    fun cyclicService_cyclic_services_provide_appropriate_dependencies() {
        val sl = ServiceLocator(esl)
        val c1 = sl.cyclicDep1
        val c2 = sl.cyclicDep2
        assertEquals(sl.cyclicDep1.sp(), sl.cyclicDep2)
        assertEquals(sl.cyclicDep2.sp(), sl.cyclicDep1)
        assertEquals(c1.sp(), c2)
        assertEquals(c2.sp(), c1)
    }

    @Test
    fun global_provides_appropriate_instances() {
        val sl = ServiceLocator(esl)
        assertEquals(sl.instance, sl.instance)
        assertEquals(sl.nonLazyInstance, sl.nonLazyInstance)
        assertEquals(sl.dependInstance, sl.dependInstance)
        assertEquals(sl.delegatedInstance, sl.delegatedInstance)
        assertNotEquals(sl.factory, sl.factory)
    }
}
package by.vkatz.leviathan

import org.junit.Assert
import org.junit.Test
import kotlin.reflect.KProperty0

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
    val overrideTest = instance { Service() }
}

//------------Code------------

@Suppress("MemberVisibilityCanBePrivate")
class Test {
    val esl = ExternalServices()

    @Test
    fun `instance # instance of service is not created before calling it`() {
        val sl = ServiceLocator(esl)
        val instance = (ServiceLocator::class.java)
            .getDeclaredField("instance\$delegate")
            .also { it.isAccessible = true }
            .get(sl)
        instance::class.java
            .getDeclaredField("service\$delegate")
            .also { it.isAccessible = true }
            .get(instance)
            .let { it as Lazy<*> }
            .isInitialized()
            .let { Assert.assertEquals(it, false) }
    }

    @Test
    fun `instance # instance of service is created after calling it`() {
        val sl = ServiceLocator(esl)
        sl.instance
        val instance = (ServiceLocator::class.java)
            .getDeclaredField("instance\$delegate")
            .also { it.isAccessible = true }
            .get(sl)
        instance::class.java
            .getDeclaredField("service\$delegate")
            .also { it.isAccessible = true }
            .get(instance)
            .let { it as Lazy<*> }
            .isInitialized()
            .let { Assert.assertEquals(it, true) }
    }

    @Test
    fun `instance # provide same objects`() {
        val sl = ServiceLocator(esl)
        val instance = sl.instance
        Assert.assertEquals(sl.instance, sl.instance)
        Assert.assertEquals(instance, instance)
    }

    @Test
    fun `factory # provide new objects on every access`() {
        val sl = ServiceLocator(esl)
        Assert.assertNotEquals(sl.factory, sl.factory)
    }

    @Test
    fun `dependInstance # use same object as used instance`() {
        val sl = ServiceLocator(esl)
        val s = sl.instance
        val dps = sl.dependInstance
        Assert.assertEquals(dps.s, s)
        Assert.assertEquals(sl.dependInstance.s, sl.instance)
    }

    @Test
    fun `delegatedInstance # provide same object and original service`() {
        val sl = ServiceLocator(esl)
        val ess = esl.service
        val dps = sl.delegatedInstance
        Assert.assertEquals(ess, dps)
        Assert.assertEquals(sl.delegatedInstance, esl.service)
    }


    @Test
    fun `cyclicService # cyclic services provide appropriate dependencies`() {
        val sl = ServiceLocator(esl)
        val c1 = sl.cyclicDep1
        val c2 = sl.cyclicDep2
        Assert.assertEquals(sl.cyclicDep1.sp(), sl.cyclicDep2)
        Assert.assertEquals(sl.cyclicDep2.sp(), sl.cyclicDep1)
        Assert.assertEquals(c1.sp(), c2)
        Assert.assertEquals(c2.sp(), c1)
    }

    @Test
    fun `global # get() provide same objects except factory`() {
        val sl = ServiceLocator(esl)
        Assert.assertEquals(sl.instance, sl.instance)
        Assert.assertEquals(sl.nonLazyInstance, sl.nonLazyInstance)
        Assert.assertEquals(sl.dependInstance, sl.dependInstance)
        Assert.assertNotEquals(sl.factory, sl.factory)
        Assert.assertEquals(sl.delegatedInstance, sl.delegatedInstance)
    }

    @Suppress("UNCHECKED_CAST")
    fun <SL : Leviathan, T> SL.provides(prop: KProperty0<T>, value: T?) {
        this::class.java
            .getDeclaredField("${prop.name}\$delegate")
            .apply { isAccessible = true }
            .get(this)
            .let { it as Leviathan.ServiceDelegate<T> }
            .provides(value)
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun `global # OVERRIDE provide appropriate object`() {
        val sl = ServiceLocator(esl)
        val s = Service()
        //hard override
        sl.provides(sl::instance, s)
        Assert.assertEquals(s, sl.instance)
        //soft override
        sl.overrideTest.provides(s)
        Assert.assertEquals(s, sl.overrideTest())
    }
}
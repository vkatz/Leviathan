package by.vkatz.leviathan

import org.junit.Assert
import org.junit.Test
import kotlin.reflect.KProperty0

const val TAG1 = "someTag1"
const val TAG2 = "someTag2"
const val TAG3 = "someTag3"
const val TAG_LOCAL = "local_tag"

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
    val taggedInstance1 by taggedInstance(TAG1) { Service() }
    val taggedInstance1clone by taggedInstance(TAG1) { Service() }
    val taggedInstance2 by taggedInstance(TAG2) { Service() }
    val dependInstance by instance { DependService(instance) }
    val factory by factory { Service() }
    val delegatedInstance = externalServices.service
    val cyclicDep1: CyclicService by instance { CyclicService { cyclicDep2 } }
    val cyclicDep2: CyclicService by instance { CyclicService { cyclicDep1 } }
    val overrideTest = instance { Service() }

    fun getTaggedService(tag: String) = taggedInstance(tag) { Service() }()
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
    fun `taggedInstance # provide same objects for same tag`() {
        val sl = ServiceLocator(esl)
        val ts = sl.getTaggedService(TAG1)
        Assert.assertEquals(sl.taggedInstance1, ts)
        Assert.assertEquals(sl.taggedInstance1, sl.taggedInstance1clone)
        Assert.assertEquals(sl.taggedInstance1, sl.getTaggedService(TAG1))
    }

    @Test
    fun `taggedInstance # provide different objects for different tags`() {
        val sl = ServiceLocator(esl)
        val ts2 = sl.getTaggedService(TAG2)
        val ts3 = sl.getTaggedService(TAG3)
        Assert.assertNotEquals(sl.taggedInstance1, sl.taggedInstance2)
        Assert.assertNotEquals(sl.taggedInstance1, ts2)
        Assert.assertNotEquals(sl.taggedInstance1, ts3)
    }

    @Test
    fun `taggedInstance # custom created tagged service provide same instance`() {
        val sl = ServiceLocator(esl)
        val ts = sl.getTaggedService(TAG_LOCAL)
        val tsCopy = sl.getTaggedService(TAG_LOCAL)
        Assert.assertEquals(ts, tsCopy)
    }

    @Test
    fun `taggedInstance # release memory by tag, direct access`() {
        val sl = ServiceLocator(esl)
        val ts = sl.taggedInstance1
        sl.releaseByTag(TAG1)
        Assert.assertNotEquals(ts, sl.taggedInstance1)
    }

    @Test
    @Suppress("UnnecessaryVariable")
    fun `taggedInstance # release memory by tag, delegate access`() {
        val sl = ServiceLocator(esl)
        val tsInstance1 = sl.taggedInstance1
        sl.releaseByTag(TAG1)
        val tsInstance2 = sl.taggedInstance1
        Assert.assertNotEquals(tsInstance1, tsInstance2)
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
        Assert.assertEquals(sl.taggedInstance1, sl.taggedInstance1)
        Assert.assertEquals(sl.taggedInstance2, sl.taggedInstance2)
        Assert.assertEquals(sl.getTaggedService("RND"), sl.getTaggedService("RND"))
        Assert.assertEquals(sl.dependInstance, sl.dependInstance)
        Assert.assertNotEquals(sl.factory, sl.factory)
        Assert.assertEquals(sl.delegatedInstance, sl.delegatedInstance)
    }

    @Suppress("UNCHECKED_CAST")
    fun <SL : Leviathan, T> SL.provides(prop : KProperty0<T>, value : T?){
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
        sl.provides(sl::instance,s)
        Assert.assertEquals(s, sl.instance)
        //soft override
        sl.overrideTest.provides(s)
        Assert.assertEquals(s, sl.overrideTest())
    }
}
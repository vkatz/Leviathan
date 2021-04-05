package by.vkatz.leviathan

import org.junit.Assert
import org.junit.Test

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

//Outer independent ServiceLocator
class ExternalServices : Leviathan() {
    val service = createInstance { Service() }
}

//Main ServiceLocator
class ServiceLocator(externalServices: ExternalServices) : Leviathan() {
    val instance = instance { Service() }
    val createdInstance = createInstance { Service() }
    val taggedInstance1 = taggedInstance(TAG1) { Service() }
    val taggedInstance1clone = taggedInstance(TAG1) { Service() }
    val taggedInstance2 = taggedInstance(TAG2) { Service() }
    val dependInstance = instance { DependService(instance()) }
    val newInstance = newInstance { Service() }
    val delegatedInstance = externalServices.service

    fun getTaggedService(tag: String) = taggedInstance(tag) { Service() }
}

//------------Code------------

@Suppress("MemberVisibilityCanBePrivate")
class Test {
    val esl = ExternalServices()

    @Test
    fun `instance # instance of service is not created before calling it`() {
        val sl = ServiceLocator(esl)
        Leviathan.LazyServiceDelegate::class.java
                .getDeclaredField("service\$delegate")
                .also { it.isAccessible = true }
                .get(sl.instance)
                .let { it as Lazy<*> }
                .isInitialized()
                .let { Assert.assertEquals(it, false) }
    }

    @Test
    fun `instance # instance of service is created after calling it`() {
        val sl = ServiceLocator(esl)
        sl.instance()
        Leviathan.LazyServiceDelegate::class.java
                .getDeclaredField("service\$delegate")
                .also { it.isAccessible = true }
                .get(sl.instance)
                .let { it as Lazy<*> }
                .isInitialized()
                .let { Assert.assertEquals(it, true) }
    }

    @Test
    fun `Instance # provide same objects`() {
        val sl = ServiceLocator(esl)
        val instance by sl.instance
        Assert.assertEquals(sl.instance(), sl.instance())
        Assert.assertEquals(instance, instance)
    }

    @Test
    fun `createInstance # provide new objects on every access`() {
        val sl = ServiceLocator(esl)
        val newInstance by sl.newInstance
        Assert.assertNotEquals(sl.newInstance(), sl.newInstance())
        Assert.assertNotEquals(newInstance, newInstance)
    }

    @Test
    fun `taggedInstance # provide same objects for same tag`() {
        val sl = ServiceLocator(esl)
        val ts by sl.getTaggedService(TAG1)
        Assert.assertEquals(sl.taggedInstance1(), ts)
        Assert.assertEquals(sl.taggedInstance1(), sl.taggedInstance1clone())
        Assert.assertEquals(sl.taggedInstance1(), sl.getTaggedService(TAG1)())
    }

    @Test
    fun `taggedInstance # provide different objects for different tags`() {
        val sl = ServiceLocator(esl)
        val ts2 by sl.getTaggedService(TAG2)
        val ts3 by sl.getTaggedService(TAG3)
        Assert.assertNotEquals(sl.taggedInstance1(), sl.taggedInstance2())
        Assert.assertNotEquals(sl.taggedInstance1(), ts2)
        Assert.assertNotEquals(sl.taggedInstance1(), ts3)
    }

    @Test
    fun `taggedInstance # custom created tagged service provide same instance`() {
        val sl = ServiceLocator(esl)
        val ts by sl.getTaggedService(TAG_LOCAL)
        val tsCopy by sl.getTaggedService(TAG_LOCAL)
        Assert.assertEquals(ts, tsCopy)
    }

    @Test
    fun `taggedInstance # release memory by tag, direct access`() {
        val sl = ServiceLocator(esl)
        val ts = sl.taggedInstance1()
        sl.releaseByTag(TAG1)
        Assert.assertNotEquals(ts, sl.taggedInstance1())
    }

    @Test
    @Suppress("UnnecessaryVariable")
    fun `taggedInstance # release memory by tag, delegate access`() {
        val sl = ServiceLocator(esl)
        val ts by sl.taggedInstance1
        val tsInstance1 = ts
        sl.releaseByTag(TAG1)
        val tsInstance2 = ts
        Assert.assertNotEquals(tsInstance1, tsInstance2)
    }

    @Test
    fun `dependInstance # use same object as used instance`() {
        val sl = ServiceLocator(esl)
        val s by sl.instance
        val dps by sl.dependInstance
        Assert.assertEquals(dps.s, s)
        Assert.assertEquals(sl.dependInstance().s, sl.instance())
    }

    @Test
    fun `delegatedInstance # provide same object and original service`() {
        val sl = ServiceLocator(esl)
        val ess by esl.service
        val dps by sl.delegatedInstance
        Assert.assertEquals(ess, dps)
        Assert.assertEquals(sl.delegatedInstance(), esl.service())
    }

    @Test
    fun `global # get() provide same objects except createInstance`() {
        val sl = ServiceLocator(esl)
        Assert.assertEquals(sl.instance(), sl.instance())
        Assert.assertEquals(sl.createdInstance(), sl.createdInstance())
        Assert.assertEquals(sl.taggedInstance1(), sl.taggedInstance1())
        Assert.assertEquals(sl.taggedInstance2(), sl.taggedInstance2())
        Assert.assertEquals(sl.getTaggedService("RND")(), sl.getTaggedService("RND")())
        Assert.assertEquals(sl.dependInstance(), sl.dependInstance())
        Assert.assertNotEquals(sl.newInstance(), sl.newInstance())
        Assert.assertEquals(sl.delegatedInstance(), sl.delegatedInstance())
    }

    @Test
    fun `global # OVERRIDE provide appropriate object`() {
        val sl = ServiceLocator(esl)
        val s = Service()
        val ps by sl.instance
        sl.instance.provides(s)
        Assert.assertEquals(s, ps)
        Assert.assertEquals(s, sl.instance())
    }
}
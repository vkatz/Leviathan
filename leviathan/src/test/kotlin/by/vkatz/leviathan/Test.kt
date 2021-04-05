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
    val dependInstance = instance { DependService(instance.get()) }
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
        sl.instance.get()
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
        Assert.assertEquals(sl.instance.get(), sl.instance.get())
        Assert.assertEquals(instance, instance)
    }

    @Test
    fun `createInstance # provide new objects on every access`() {
        val sl = ServiceLocator(esl)
        val newInstance by sl.newInstance
        Assert.assertNotEquals(sl.newInstance.get(), sl.newInstance.get())
        Assert.assertNotEquals(newInstance, newInstance)
    }

    @Test
    fun `taggedInstance # provide same objects for same tag`() {
        val sl = ServiceLocator(esl)
        val ts by sl.getTaggedService(TAG1)
        Assert.assertEquals(sl.taggedInstance1.get(), ts)
        Assert.assertEquals(sl.taggedInstance1.get(), sl.taggedInstance1clone.get())
        Assert.assertEquals(sl.taggedInstance1.get(), sl.getTaggedService(TAG1).get())
    }

    @Test
    fun `taggedInstance # provide different objects for different tags`() {
        val sl = ServiceLocator(esl)
        val ts2 by sl.getTaggedService(TAG2)
        val ts3 by sl.getTaggedService(TAG3)
        Assert.assertNotEquals(sl.taggedInstance1.get(), sl.taggedInstance2.get())
        Assert.assertNotEquals(sl.taggedInstance1.get(), ts2)
        Assert.assertNotEquals(sl.taggedInstance1.get(), ts3)
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
        val ts = sl.taggedInstance1.get()
        sl.releaseByTag(TAG1)
        Assert.assertNotEquals(ts, sl.taggedInstance1.get())
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
        Assert.assertEquals(sl.dependInstance.get().s, sl.instance.get())
    }

    @Test
    fun `delegatedInstance # provide same object and original service`() {
        val sl = ServiceLocator(esl)
        val ess by esl.service
        val dps by sl.delegatedInstance
        Assert.assertEquals(ess, dps)
        Assert.assertEquals(sl.delegatedInstance.get(), esl.service.get())
    }

    @Test
    fun `global # get() provide same objects except createInstance`() {
        val sl = ServiceLocator(esl)
        Assert.assertEquals(sl.instance.get(), sl.instance.get())
        Assert.assertEquals(sl.createdInstance.get(), sl.createdInstance.get())
        Assert.assertEquals(sl.taggedInstance1.get(), sl.taggedInstance1.get())
        Assert.assertEquals(sl.taggedInstance2.get(), sl.taggedInstance2.get())
        Assert.assertEquals(sl.getTaggedService("RND").get(), sl.getTaggedService("RND").get())
        Assert.assertEquals(sl.dependInstance.get(), sl.dependInstance.get())
        Assert.assertNotEquals(sl.newInstance.get(), sl.newInstance.get())
        Assert.assertEquals(sl.delegatedInstance.get(), sl.delegatedInstance.get())
    }

    @Test
    fun `global # OVERRIDE provide appropriate object`() {
        val sl = ServiceLocator(esl)
        val s = Service()
        val ps by sl.instance
        sl.instance.provides(s)
        Assert.assertEquals(s, ps)
        Assert.assertEquals(s, sl.instance.get())
    }
}
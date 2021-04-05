package by.vkatz.leviathan

import java.util.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

open class Leviathan {

    private val instanceTagServices = HashMap<String, Any?>()

    //provide functions

    fun <T> newInstance(creator: () -> T): ServiceDelegate<T> {
        return ProvidableServiceDelegate { creator() }
    }

    fun <T> createInstance(creator: () -> T): ServiceDelegate<T> {
        val service = creator()
        return ProvidableServiceDelegate { service }
    }

    fun <T> taggedInstance(tag: String, creator: () -> T): ServiceDelegate<T> {
        return ProvidableServiceDelegate {
            @Suppress("UNCHECKED_CAST")
            var service: T? = instanceTagServices[tag] as? T
            if (service == null) {
                service = creator()
                instanceTagServices[tag] = service
            }
            service!!
        }
    }

    fun <T> instance(creator: () -> T): ServiceDelegate<T> {
        return LazyServiceDelegate(creator)
    }

    fun releaseByTag(tag: String) {
        instanceTagServices.remove(tag)
    }

    //support classes

    interface ServiceDelegate<T> : ReadOnlyProperty<Any?, T> {
        fun provides(t: T?)
        fun get(): T
    }

    abstract class AbsServiceDelegate<T> : ServiceDelegate<T> {
        protected var providable: T? = null

        override fun provides(t: T?) {
            providable = t
        }

        override fun getValue(thisRef: Any?, property: KProperty<*>): T = get()
    }

    class LazyServiceDelegate<T>(provider: () -> T) : AbsServiceDelegate<T>() {
        private val service by lazy(provider)
        override fun get() = providable ?: service
    }

    class ProvidableServiceDelegate<T>(val provider: () -> T) : AbsServiceDelegate<T>() {
        override fun get() = providable ?: provider()
    }
}
package by.vkatz.leviathan

import java.util.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Service locator base class
 *
 * !!!WARNING!!!
 *
 * all instance methods provide !!!DELEGATES!!! and not an objects itself
 *
 * !!!WARNING!!!
 *
 * Usage:
 *
 * 1) extend class from Leviathan
 * 2) create services delegates by using one of the provided helpers
 * 3) use delegates within 'by' or '.invoke() / ()'
 *
 *
 * Example:
 * ```
 * class Service(){
 *      fun doSmth(){ ... }
 * }
 *
 * class ServiceLocator : Leviathan() {
 *      val service = instance { Service() }
 * }
 *
 * class Model(val sl:ServiceLocator = ServiceLocator()){
 *      val service by sl.service
 *
 *      fun foo(){
 *          service.doSmth()
 *          sl.service().doSmth()
 *      }
 * }
 * ```
 */
open class Leviathan {

    private val instanceTagServices = HashMap<String, Any?>()

    //----- Providers -----

    /**
     * Create a delegate to provide new service per each request
     *
     * @param creator factory method to create service
     */
    fun <T> newInstance(creator: () -> T): ServiceDelegate<T> {
        return ProvidableServiceDelegate { creator() }
    }

    /**
     * Create a delegate to provide same service per all request
     *
     * Service will be created immediately
     *
     * @param creator factory method to create service
     */
    fun <T> createInstance(creator: () -> T): ServiceDelegate<T> {
        val service = creator()
        return ProvidableServiceDelegate { service }
    }

    /**
     * Create a delegate to provide same services per request with same tag
     *
     * Service will be created lazily
     *
     * Provide same services (do not misplace with service delegate) for same tags
     * until you call [releaseByTag] for specific tag
     *
     * @param tag the tag that going to be associated to created instances of service
     * @param creator factory method to create service
     */
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

    /**
     * Create a delegate to provide same services per request
     *
     * Service will be created lazily
     *
     * @param creator factory method to create service objects
     */
    fun <T> instance(creator: () -> T): ServiceDelegate<T> {
        return LazyServiceDelegate(creator)
    }

    //----- Utils -----

    /**
     * Release associated service
     *
     * Calling the delegate associated to relative tag will generate new service
     * instance upon next call
     */
    fun releaseByTag(tag: String) {
        instanceTagServices.remove(tag)
    }

    //----- Helpers -----

    /**
     * Service delegate object, provides appropriate service by calling [invoke] in the function body
     *
     * ```
     * fun foo(){
     *     val service = delegate()
     * }
     * ```
     *
     * or delegate usage via "by" construction
     *
     * ```
     * class Foo{
     *      val service by delegate
     * }
     * ```
     *
     * Allow to override providable object via [provides]
     */
    interface ServiceDelegate<T> : ReadOnlyProperty<Any?, T> {
        /**
         * Force the delegate to provide specific object
         *
         * Set to `null` to provide original object
         */
        fun provides(t: T?)

        /**
         * Return the delegated service instance
         */
        operator fun invoke(): T
    }

    /**
     * Impl of delegate & override methods
     */
    abstract class AbsServiceDelegate<T> : ServiceDelegate<T> {
        protected var providable: T? = null

        override fun provides(t: T?) {
            providable = t
        }

        override fun getValue(thisRef: Any?, property: KProperty<*>): T = invoke()
    }

    /**
     * Lazy service implementation
     *
     * Use provider once to init lazy property
     */
    class LazyServiceDelegate<T>(provider: () -> T) : AbsServiceDelegate<T>() {
        private val service by lazy(provider)
        override operator fun invoke() = providable ?: service
    }

    /**
     * Simple service delegate
     *
     * Use provider for all calls
     */
    class ProvidableServiceDelegate<T>(val provider: () -> T) : AbsServiceDelegate<T>() {
        override operator fun invoke() = providable ?: provider()
    }
}
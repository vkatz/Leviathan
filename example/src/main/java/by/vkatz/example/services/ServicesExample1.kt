package by.vkatz.example.services

import androidx.lifecycle.ViewModel
import by.vkatz.leviathan.Leviathan

class Service1

interface Service2
class Service2Impl : Service2

open class Services : Leviathan() {
    val service1InstanceDelegate = instance { Service1() }
    val service1InstanceProvider by instance { Service1() }
    val service1FactoryDelegate = factory { Service1() }
    val service1FactoryProvider by factory { Service1() }

    val service2InstanceDelegate = instance<Service2> { Service2Impl() } //hide impl example
}

object ServicesImpl : Services()

/**
 * Info
 * ```
 * • Services prop is easy to override during tests
 * • You can use direct ServiceImpl object access instead of passin it as prop
 * • You may skip making Service + ServiceImpl and make `object Services` immediately
 * ```
 */
class Services1Model(services: Services = ServicesImpl) : ViewModel() {
    //below the variants of accessing:
    val sid1 by services.service1InstanceDelegate  //same obj every access,    same   objects for different instances of model, redirect to delegate
    val sid2 = services.service1InstanceDelegate() //same obj every access,    same   objects for different instances of model, stored inside model  =>recommended<==
    val sip = services.service1InstanceProvider    //same obj every access,    same   objects for different instances of model, stored inside model
    val sfd1 by services.service1FactoryDelegate   //new  obj every access, different objects for different instances of model, will call a delegate create function
    val sfd2 = services.service1FactoryDelegate()  //same obj every access, different objects for different instances of model, stored inside model
    val sfp = services.service1FactoryProvider     //same obj every access, different objects for different instances of model, stored inside model

    val s2 = services.service2InstanceDelegate() // type is Service2, impl is Service2Impl

    fun getRefs() = "Services1Model:\n" +
            listOf(
                "sid1 -> $sid1",
                "sid2 -> $sid2",
                "sip  -> $sip",
                "sfd1 -> $sfd1",
                "sfd2 -> $sfd2",
                "sfp  -> $sfp",
                "s2   -> $s2",
                  )
                .joinToString("\n")
}

@Suppress("unused")
class TestExample{

    fun someTest(){
        val services = Services()
        val s1 = Service1()
        services.service1InstanceDelegate.provides(s1)
        val model = Services1Model(services)
        //do tests with model within the overridden service
    }
}
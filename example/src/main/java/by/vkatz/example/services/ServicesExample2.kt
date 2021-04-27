package by.vkatz.example.services

import androidx.lifecycle.ViewModel
import by.vkatz.leviathan.inject.LeviathanFactory
import by.vkatz.leviathan.inject.LeviathanInstance
import by.vkatz.leviathan.inject.LeviathanService

//--------service bases-------------

@LeviathanService //no specific name - will be used default
interface LeviathanServiceBase

@LeviathanService("LeviathanCustomServiceImpl") //specific name
interface LeviathanServiceCustom

//--------instance-------------

//regular
@LeviathanInstance
class ServiceInstanceSimple

//custom prop name
@LeviathanInstance(propertyName = "myProp1")
class ServiceInstancePropName

//non lazy impl
@LeviathanInstance(lazy = false)
class ServiceInstanceNonLazy

//provide as interface
interface ServiceInstanceWithInterface

@LeviathanInstance(provideAs = ServiceInstanceWithInterface::class)
class ServiceInstanceWithInterfaceImpl : ServiceInstanceWithInterface

//non default service target
@LeviathanInstance(provideBy = LeviathanServiceCustom::class)
class ServiceInstanceCustomServiceTarget

//--------factory-------------

//regular
@LeviathanFactory
class ServiceFactorySimple

//custom prop name
@LeviathanFactory(propertyName = "myProp2")
class ServiceFactoryPropName

//provide as interface
interface ServiceFactoryWithInterface

@LeviathanFactory(provideAs = ServiceFactoryWithInterface::class)
class ServiceFactoryWithInterfaceImpl : ServiceFactoryWithInterface

//non default service target
@LeviathanFactory(provideBy = LeviathanServiceCustom::class)
class ServiceFactoryCustomServiceTarget

//------------other-------------

//nested
class ServiceHolder {
    @LeviathanInstance
    class ServiceFactoryNested

    @LeviathanFactory
    class ServiceInstanceNested
}

//depend service
@LeviathanInstance
class ServiceWithDependency constructor(
    val s1: ServiceInstanceSimple = LeviathanServices.serviceInstanceSimple(),
    val s2: ServiceHolder.ServiceFactoryNested = LeviathanServices.serviceFactoryNested(),
    val s3: ServiceFactoryPropName = LeviathanServices.myProp2(),
) {
    val s4: ServiceFactorySimple by LeviathanServices.serviceFactorySimple
    val s5: ServiceHolder.ServiceInstanceNested = LeviathanServices.serviceInstanceNested()
    val s6: ServiceFactoryPropName = LeviathanServices.myProp2()
}

//------------usage-------------

class Services2Model(
    private val serviceInstanceSimple: ServiceInstanceSimple = LeviathanServices.serviceInstanceSimple(),
    private val serviceInstancePropName: ServiceInstancePropName = LeviathanServices.myProp1(), //custom prop access
    private val serviceInstanceNonLazy: ServiceInstanceNonLazy = LeviathanServices.serviceInstanceNonLazy(),
    private val serviceInstanceWithInterfaceImpl: ServiceInstanceWithInterface = LeviathanServices.serviceInstanceWithInterfaceImpl(), //realization is hidden, type is interface
    private val serviceInstanceCustomServiceTarget: ServiceInstanceCustomServiceTarget = LeviathanCustomServiceImpl.serviceInstanceCustomServiceTarget(), //custom service access
    private val serviceFactorySimple: ServiceFactorySimple = LeviathanServices.serviceFactorySimple(), //new for each model as it provided via factory
    private val serviceFactoryPropName: ServiceFactoryPropName = LeviathanServices.myProp2(),
    private val serviceFactoryWithInterfaceImpl: ServiceFactoryWithInterface = LeviathanServices.serviceFactoryWithInterfaceImpl(),
    private val serviceFactoryCustomServiceTarget: ServiceFactoryCustomServiceTarget = LeviathanCustomServiceImpl.serviceFactoryCustomServiceTarget(),
    private val serviceFactoryNested: ServiceHolder.ServiceFactoryNested = LeviathanServices.serviceFactoryNested(),
    private val serviceInstanceNested: ServiceHolder.ServiceInstanceNested = LeviathanServices.serviceInstanceNested(),
    private val serviceWithDependency: ServiceWithDependency = LeviathanServices.serviceWithDependency(),
) : ViewModel() {
    fun getRefs() = "Services1Model:\n" +
            listOf(
                "InstanceSimple               -> $serviceInstanceSimple",
                "InstancePropName             -> $serviceInstancePropName",
                "InstanceNonLazy              -> $serviceInstanceNonLazy",
                "InstanceWithInterfaceImpl    -> $serviceInstanceWithInterfaceImpl",
                "InstanceCustomServiceTarget  -> $serviceInstanceCustomServiceTarget",
                "FactorySimple                -> $serviceFactorySimple",
                "FactoryPropName              -> $serviceFactoryPropName",
                "FactoryWithInterfaceImpl     -> $serviceFactoryWithInterfaceImpl",
                "FactoryCustomServiceTarget   -> $serviceFactoryCustomServiceTarget",
                "FactoryNested                -> $serviceFactoryNested",
                "InstanceNested               -> $serviceInstanceNested",
                "WithDependency               -> $serviceWithDependency",
            )
                .joinToString("\n")
}

//------------testing-------------

@Suppress("unused")
class Test2Example {

    fun someTest() {
        val service = ServiceInstanceSimple()
        val model = Services2Model(serviceInstanceSimple = service)
        //do tests with model within the overridden service
    }
}
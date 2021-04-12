package by.vkatz.example.services

import androidx.lifecycle.ViewModel
import by.vkatz.leviathan.inject.LeviathanService
import by.vkatz.leviathan.inject.LeviathanServicePackage
import by.vkatz.leviathan.inject.ProvideBy

@LeviathanServicePackage
private val stub = Unit


//regular
@LeviathanService
class ServiceSimple

//nested
class ServiceHolder {
    @LeviathanService
    class ServiceNested
}

//custom file
@LeviathanService(serviceName = "CustomServices")
class ServiceCustomServiceName

//custom file
@LeviathanService(propertyName = "myProp")
class ServiceCustomPropertyName

//instance + provide mode
@LeviathanService(provideBy = ProvideBy.Instance)
class ServiceInstance

//factory + provide mode
@LeviathanService(provideBy = ProvideBy.Factory)
class ServiceFactory

//depend service
@LeviathanService
class ServiceWithDependency constructor(
    val s1: ServiceSimple = LeviathanServices.serviceSimple(),
    val s2: ServiceHolder.ServiceNested = LeviathanServices.serviceNested(),
) {
    val s3: ServiceFactory by LeviathanServices.serviceFactory
    val s4: ServiceCustomPropertyName = LeviathanServices.myProp()
}

class Services2Model(
    private val simple: ServiceSimple = LeviathanServices.serviceSimple(),
    private val nested: ServiceHolder.ServiceNested = LeviathanServices.serviceNested(),
    private val customServiceName: ServiceCustomServiceName = CustomServices.serviceCustomServiceName(),  //using of custom service class
    private val customPropertyName: ServiceCustomPropertyName = LeviathanServices.myProp(), //we are using custom prop
    private val instance: ServiceInstance = LeviathanServices.serviceInstance(),
    private val factory: ServiceFactory = LeviathanServices.serviceFactory(),   //will be new for each instance of model as it declared as "provideBy = ProvideBy.Factory"
    private val withDependency: ServiceWithDependency = LeviathanServices.serviceWithDependency(),

    ) : ViewModel() {
    fun getRefs() = "Services1Model:\n" +
            listOf(
                "simple             -> $simple",
                "nested             -> $nested",
                "customServiceName  -> $customServiceName",
                "customPropertyName -> $customPropertyName",
                "instance           -> $instance",
                "factory            -> $factory",
                "withDependency     -> $withDependency",
            )
                .joinToString("\n")
}

@Suppress("unused")
class Test2Example {

    fun someTest() {
        val service = ServiceSimple()
        val model = Services2Model(simple = service)
        //do tests with model within the overridden service
    }
}
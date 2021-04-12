@file:LeviathanServicePackage

package by.vkatz.example.services

import by.vkatz.leviathan.inject.LeviathanService
import by.vkatz.leviathan.inject.LeviathanServicePackage
import by.vkatz.leviathan.inject.ProvideBy


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

//instance + provide mode
@LeviathanService(provideBy = ProvideBy.Instance)
class ServiceInstance

//factory + provide mode
@LeviathanService(provideBy = ProvideBy.Factory)
class ServiceFactory

//depend service
@LeviathanService
class ServiceWithDependency constructor(
    val s1: ServiceInstance,
    val s2: ServiceInstance,
                                       ) {
    lateinit var s3: ServiceInstance
}
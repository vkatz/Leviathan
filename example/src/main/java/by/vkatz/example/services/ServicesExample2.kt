package by.vkatz.example.services

import by.vkatz.leviathan.inject.LeviathanService
import by.vkatz.leviathan.inject.ProvideAs
import by.vkatz.leviathan.inject.ProvideBy

//regular
@LeviathanService
class ServiceSimple

//nested
class ServiceHolder{
    @LeviathanService
    class ServiceNested
}

//custom file
@LeviathanService(serviceName = "MyServices")
class ServiceCustomServiceName

//instance + provide mode
@LeviathanService(provideBy = ProvideBy.Instance)
class ServiceInstance

@LeviathanService(provideBy = ProvideBy.Instance, provideAs = ProvideAs.Property)
class ServiceInstanceAsProperty

@LeviathanService(provideBy = ProvideBy.Instance, provideAs = ProvideAs.Delegate)
class ServiceInstanceAsDelegate

//factory + provide mode
@LeviathanService(provideBy = ProvideBy.Factory)
class ServiceFactory

@LeviathanService(provideBy = ProvideBy.Factory, provideAs = ProvideAs.Property)
class ServiceFactoryAsProperty

@LeviathanService(provideBy = ProvideBy.Factory, provideAs = ProvideAs.Delegate)
class ServiceFactoryAsDelegate

//depend service
@LeviathanService
class ServiceWithDependency constructor(
    val s1: ServiceInstance,
    val s2: ServiceInstance,
) {
    lateinit var s3: ServiceInstance
}
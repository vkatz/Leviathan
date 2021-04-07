package by.vkatz.leviathan.inject

@LeviathanService
class Service1

@LeviathanService(mode = ServiceMode.Factory)
class Service2

@LeviathanService(serviceName = "MyServices")
class Service3

@LeviathanService
class Service4(
    val s1: Service1 = inject(),
    val s2: Service2 = inject(),
) {
    val s3: Service3 = inject("MyServices")
}


fun foo() {
    val s4: Service4 = inject()
}
package by.vkatz.leviathan.inject

enum class ProvideBy(internal val keyWorld: String) {
    Instance("instance"),
    Factory("factory")
}

@Repeatable
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class LeviathanService(
    val serviceName: String = "LeviathanServices",
    val propertyName: String = "",
    val provideBy: ProvideBy = ProvideBy.Instance
)

@Retention(AnnotationRetention.SOURCE)
annotation class LeviathanServicePackage
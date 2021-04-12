package by.vkatz.leviathan.inject

enum class ProvideBy { Instance, Factory }

@Repeatable
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class LeviathanService(
    val serviceName: String = "LeviathanServices",
    val propertyName: String = "",
    val provideBy: ProvideBy = ProvideBy.Instance
                                 )


@Target(AnnotationTarget.FILE)
@Retention(AnnotationRetention.SOURCE)
annotation class LeviathanServicePackage
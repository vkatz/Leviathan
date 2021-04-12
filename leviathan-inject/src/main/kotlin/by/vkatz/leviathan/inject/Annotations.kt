package by.vkatz.leviathan.inject

enum class ProvideBy { Instance, Factory }
enum class ProvideAs { Property, Delegate }

@Repeatable
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class LeviathanService(
    val serviceName: String = LEVIATHAN_DEFAULT_SERVICE_NAME,
    val propertyName: String = "",
    val provideBy: ProvideBy = ProvideBy.Instance,
    val provideAs: ProvideAs = ProvideAs.Property,
) {
    companion object {
        const val LEVIATHAN_DEFAULT_SERVICE_NAME = "LeviathanService"
    }
}
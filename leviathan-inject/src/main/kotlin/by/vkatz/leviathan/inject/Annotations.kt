package by.vkatz.leviathan.inject

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class LeviathanService(
    val className: String = ""
)

@Repeatable
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class LeviathanInstance(
    val propertyName: String = "",
    val lazy: Boolean = true,
    val provideAs: KClass<*> = LeviathanService::class,
    val provideBy: KClass<*> = LeviathanService::class,
)

@Repeatable
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class LeviathanFactory(
    val propertyName: String = "",
    val provideAs: KClass<*> = LeviathanService::class,
    val provideBy: KClass<*> = LeviathanService::class,
)
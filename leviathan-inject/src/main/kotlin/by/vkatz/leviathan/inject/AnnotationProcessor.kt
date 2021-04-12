package by.vkatz.leviathan.inject

import by.vkatz.leviathan.Leviathan
import com.google.auto.service.AutoService
import java.io.File
import java.util.*
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic
import kotlin.collections.ArrayList

private const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"

@Suppress("unused")
@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions(KAPT_KOTLIN_GENERATED_OPTION_NAME)
class AnnotationProcessor : AbstractProcessor() {
    override fun getSupportedAnnotationTypes(): MutableSet<String> = mutableSetOf(LeviathanService::class.java.name, LeviathanServicePackage::class.java.name)

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

    override fun process(set: MutableSet<out TypeElement>, roundEnvironment: RoundEnvironment): Boolean {
        //env setup
        if (set.isEmpty()) return true
        val servicesPackageSources = roundEnvironment.getElementsAnnotatedWith(LeviathanServicePackage::class.java)
        val servicesPackage = servicesPackageSources
            ?.takeIf { it.count() == 1 }
            ?.first()
            ?.let { processingEnv.elementUtils.getPackageOf(it) }
            ?.qualifiedName
            ?.toString()
            ?: run {
                processingEnv.messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    """|There is ${if (servicesPackageSources.isNullOrEmpty()) "no" else "multiple"} ServicePackage defined
                       |Please define one item with @LeviathanServicePackage annotation, the package of item will be target to generate Leviathan services
                       |Example:
                       |@LeviathanServicePackage
                       |private val stub = Unit
                    """.trimMargin().replace("\n", "</br>")
                )
                throw RuntimeException()
            }
        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
        val genFolder = File(kaptKotlinGeneratedDir, servicesPackage.replace(".", File.separator))
        genFolder.mkdirs()
        //generate services
        roundEnvironment
            .getElementsAnnotatedWith(LeviathanService::class.java)
            .groupBy { it.getAnnotation(LeviathanService::class.java).serviceName }
            .forEach { generateService(genFolder, servicesPackage, it.value) }
        return true
    }

    private fun generateService(folder: File, packageName: String, services: List<Element>) {
        val serviceName = services.first().getAnnotation(LeviathanService::class.java).serviceName
        val file = File(folder, "$serviceName.kt")

        val servicesLines = ArrayList<String>()

        services.forEach {
            val annotation = it.getAnnotation(LeviathanService::class.java)
            val name = it.asType().toString()
            val property = annotation.propertyName.takeIf { n -> n.isNotBlank() } ?: it.simpleName.toString().decapitalize(Locale.getDefault())
            val serviceLine = "|\tval $property = ${annotation.provideBy.keyWorld} { $name() }"
            servicesLines.add(serviceLine)
        }

        val content = """
            |package $packageName
            |
            |import ${Leviathan::class.qualifiedName}
            |import kotlin.reflect.KClass
            |
            |object $serviceName : Leviathan(){
            |${"\t"}//services
            ${servicesLines.joinToString("\n")}
            |}
        """.trimMargin()
        file.writeText(content)
    }
}
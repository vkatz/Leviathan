package by.vkatz.leviathan.inject

import by.vkatz.leviathan.Leviathan
import com.google.auto.service.AutoService
import java.io.File
import java.util.*
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException
import javax.tools.Diagnostic
import kotlin.collections.ArrayList

private const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"

@Suppress("unused")
@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions(KAPT_KOTLIN_GENERATED_OPTION_NAME)
class AnnotationProcessor : AbstractProcessor() {
    companion object {
        private const val DEFAULT_SERVICE_NAME = "LeviathanServices"
    }

    private val defaultTargetType by lazy { processingEnv.elementUtils.getTypeElement(LeviathanService::class.java.canonicalName).asType() }

    // -------------core-------------

    override fun getSupportedAnnotationTypes(): MutableSet<String> = mutableSetOf(
        LeviathanService::class.java.name,
        LeviathanInstance::class.java.name,
        LeviathanFactory::class.java.name,
    )

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

    override fun process(set: MutableSet<out TypeElement>, roundEnvironment: RoundEnvironment): Boolean {
        //env setup
        if (set.isEmpty()) return true
        val infos = obtainServicesGenInfo(roundEnvironment)
        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
        if (kaptKotlinGeneratedDir == null) {
            log(Diagnostic.Kind.ERROR, "No generate dir defined")
            throw RuntimeException()
        }
        val rootDir = File(kaptKotlinGeneratedDir)
        infos.forEach { generateService(rootDir, it) }
        return true
    }

    // -------------generation-------------

    private fun validateServicesCount(services: List<Element>) {
        if (services.isEmpty()) {
            log(
                Diagnostic.Kind.ERROR,
                """|There is no services defined, please define at least one service
                   |Example:
                   |@LeviathanService
                   |interface AppServices
                """.trimMargin()
            )
            throw RuntimeException()
        }
    }

    private fun validateServicesTypes(services: List<Element>) {
        val isAllServiceValid = services.all {
            if (it.enclosedElements.size > 0) {
                log(Diagnostic.Kind.ERROR, "Invalid service declaration", it)
                false
            } else true
        }
        if (!isAllServiceValid) {
            log(
                Diagnostic.Kind.ERROR,
                """
                |All services should be presented as interface without methods"
                |Example:
                |@LeviathanService
                |interface AppServices
                """.trimMargin()
            )
            throw RuntimeException()
        }
    }

    private fun obtainServicesGenInfo(env: RoundEnvironment): List<ServiceGenInfo> {
        val services = env.getElementsAnnotatedWith(LeviathanService::class.java).toList()

        //validate
        validateServicesCount(services)
        validateServicesTypes(services)

        //collect services info
        val defaultServicesCount = services.count { it.getServiceRawName().isBlank() }
        if (defaultServicesCount > 1) log(Diagnostic.Kind.WARNING, "There is more than 1 unnamed service defined, default $DEFAULT_SERVICE_NAME will not be generated")
        val servicesData = services.map {
            val rawName = it.getServiceRawName()
            val name = when {
                rawName.isNotBlank() -> rawName
                rawName.isBlank() && defaultServicesCount == 1 -> DEFAULT_SERVICE_NAME
                else -> "${it.simpleName}Impl"
            }
            ServiceGenInfo(it, name, ArrayList())
        }
        val defaultService = servicesData.firstOrNull { it.serviceName == DEFAULT_SERVICE_NAME }

        //collect elements info
        val elementsInfo = ArrayList<Pair<Element?, ElementGenerationData>>()
        elementsInfo += env.getElementsAnnotatedWith(LeviathanInstance::class.java).map {
            val annotation = it.getAnnotation(LeviathanInstance::class.java)
            mirroredType { annotation.provideBy } to ElementGenerationData(it, annotation)
        }
        elementsInfo += env.getElementsAnnotatedWith(LeviathanFactory::class.java).map {
            val annotation = it.getAnnotation(LeviathanFactory::class.java)
            mirroredType { annotation.provideBy } to ElementGenerationData(it, annotation)
        }

        //join elements to services
        elementsInfo.forEach {
            val target =
                if (it.first?.asType() == defaultTargetType) defaultService
                else servicesData.firstOrNull { s -> s.serviceOwner == it.first }
            if (target == null) {
                log(Diagnostic.Kind.ERROR, "Targeted service not found", it.second.element)
                throw RuntimeException()
            }
            target.services.add(it.second)
        }
        return servicesData
    }

    private fun getInstanceDeclaration(element: Element): String {
        val annotation = element.getAnnotation(LeviathanInstance::class.java)
        val className = element.asType().toString()
        val provideAsName = run {
            val outType = mirroredType { annotation.provideAs }
            if (outType == null || outType.asType() == defaultTargetType) className
            else outType.asType().toString()
        }
        val lazy = annotation.lazy
        val propertyName = annotation.propertyName.takeIf { n -> n.isNotBlank() } ?: element.simpleName.toString().decapitalize(Locale.ROOT)
        return listOfNotNull(
            "|\tval $propertyName = instance ",
            if (provideAsName != className) "<$provideAsName> " else null,
            if (!lazy) "(false) " else null,
            "{ $className() }",
        ).joinToString("")
    }

    private fun getFactoryDeclaration(element: Element): String {
        val annotation = element.getAnnotation(LeviathanFactory::class.java)
        val className = element.asType().toString()
        val provideAsName = run {
            val outType = mirroredType { annotation.provideAs }
            if (outType == null || outType.asType() == defaultTargetType) className
            else outType.asType().toString()
        }
        val propertyName = annotation.propertyName.takeIf { n -> n.isNotBlank() } ?: element.simpleName.toString().decapitalize(Locale.ROOT)
        return listOfNotNull(
            "|\tval $propertyName = factory ",
            if (provideAsName != className) "<$provideAsName> " else null,
            "{ $className() }",
        ).joinToString("")
    }

    private fun generateService(rootDir: File, info: ServiceGenInfo) {
        val packageName = processingEnv.elementUtils.getPackageOf(info.serviceOwner).qualifiedName.toString()
        val parentName = info.serviceOwner.asType().toString()
        val serviceName = info.serviceName
        val file = File(rootDir, "$serviceName.kt")

        val servicesLines = ArrayList<String>()
        info.services.forEach {
            val line = when (it.annotation) {
                is LeviathanInstance -> getInstanceDeclaration(it.element)
                is LeviathanFactory -> getFactoryDeclaration(it.element)
                else -> null
            }
            if (line == null) {
                log(Diagnostic.Kind.ERROR, "Unable to generate code", it.element)
                throw RuntimeException()
            }
            servicesLines.add(line)
        }

        val content = """
              |package $packageName
              |
              |import ${Leviathan::class.qualifiedName}
              |
              |object $serviceName : ${Leviathan::class.simpleName}(), $parentName {
              ${servicesLines.joinToString("\n")}
              |}
          """.trimMargin()
        file.writeText(content)
    }

    // -------------helpers-------------

    private fun log(kind: Diagnostic.Kind, message: String, element: Element? = null) {
        processingEnv.messager.printMessage(kind, message, element)
    }

    private fun mirroredType(action: () -> Any?): Element? {
        return try {
            action()
            null
        } catch (e: MirroredTypeException) {
            processingEnv.typeUtils.asElement(e.typeMirror)
        } catch (e: Throwable) {
            null
        }
    }

    private fun Element.getServiceRawName() = getAnnotation(LeviathanService::class.java).className

    private data class ElementGenerationData(
        val element: Element,
        val annotation: Annotation
    )

    private data class ServiceGenInfo(
        val serviceOwner: Element,
        val serviceName: String,
        val services: ArrayList<ElementGenerationData>
    )
}
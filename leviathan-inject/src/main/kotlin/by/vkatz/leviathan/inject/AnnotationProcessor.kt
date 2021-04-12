package by.vkatz.leviathan.inject

import com.google.auto.service.AutoService
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

@Suppress("unused")
@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class AnnotationProcessor : AbstractProcessor() {
    override fun getSupportedAnnotationTypes(): MutableSet<String> = mutableSetOf(LeviathanService::class.java.name, LeviathanServicePackage::class.java.name)

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

    override fun process(set: MutableSet<out TypeElement>?, roundEnvironment: RoundEnvironment?): Boolean {
        val packageName = roundEnvironment
            ?.getElementsAnnotatedWith(LeviathanServicePackage::class.java)
            ?.takeIf { it.count() == 1 }
            ?.first()
            ?.let { println("AAAAAAAAA AMAAAAAA ALIIIIIIVEEEEEE $it") }



//        roundEnvironment
//            ?.getElementsAnnotatedWith(LeviathanService::class.java)
//            ?.mapNotNull { it as? Symbol.ClassSymbol }
//            ?.groupBy { it.getAnnotation(LeviathanService::class.java).serviceName }
//            ?.forEach { generateService(it.value) }
        return true
    }

//    private fun generateService(services: List<Symbol.ClassSymbol>) {
//        val fileName = services.first().getAnnotation(LeviathanService::class.java).serviceName
//        val filePackage = services
//            .map { it.owner as? Symbol.PackageSymbol }
//        val fileContent = "//todo ${fileName}"

        //gen content (use Kotlin Poet ? )

        //write content
//        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
//        val file = File(kaptKotlinGeneratedDir, "$fileName.kt")

//        file.writeText(fileContent)
//    }
}
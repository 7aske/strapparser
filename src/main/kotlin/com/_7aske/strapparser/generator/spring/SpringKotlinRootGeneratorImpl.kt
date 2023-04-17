package com._7aske.strapparser.generator.spring

import com._7aske.strapparser.generator.DataTypeResolver
import com._7aske.strapparser.generator.GeneratorContext
import com._7aske.strapparser.generator.kotlin.Formatter
import com._7aske.strapparser.generator.kotlin.KotlinClassGenerator
import com._7aske.strapparser.util.snakeCaseToCamelCase
import java.nio.file.Path
import java.nio.file.Paths

class SpringKotlinRootGeneratorImpl(
    ctx: GeneratorContext,
    dataTypeResolver: DataTypeResolver
) : KotlinClassGenerator(ctx, dataTypeResolver) {

    private val formatter = Formatter()

    init {
        imports.remove("java.util.*")
        imports.remove("java.time.*")
        import("org.springframework.boot.SpringApplication")
        import("org.springframework.boot.runApplication")
        import("org.springframework.boot.autoconfigure.SpringBootApplication")
    }

    override fun getClassName(): String {
        val name = snakeCaseToCamelCase(ctx.args.name)
        return "${name}Application"
    }

    override fun getPackage(): String =
        ctx.getPackageName()

    override fun getOutputFilePath(): Path = Paths.get(
        ctx.getOutputLocation(),
        "src",
        "main",
        "kotlin",
        getPackage().replace(".", separator),
        this.getClassName() + ".kt"
    )

    override fun generate(): String =
        formatter.formatSource(
            buildString {
                appendLine("package ${getPackage()}")
                appendLine(getImports())
                appendLine("@SpringBootApplication")
                appendLine("open class ${getClassName()}")
                appendLine("fun main(args: Array<String>) {")
                appendLine("SpringApplication.run(${getClassName()}::class.java, *args)")
                appendLine("}")
            }
        )
}

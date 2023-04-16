package com._7aske.strapparser.generator.spring

import com._7aske.strapparser.generator.DataTypeResolver
import com._7aske.strapparser.generator.GeneratorContext
import com._7aske.strapparser.generator.java.JavaClassGenerator
import com._7aske.strapparser.util.snakeCaseToCamelCase
import com.google.googlejavaformat.java.Formatter
import java.nio.file.Path
import java.nio.file.Paths

class SpringJavaRootGeneratorImpl(
    ctx: GeneratorContext,
    dataTypeResolver: DataTypeResolver
) : JavaClassGenerator(ctx, dataTypeResolver) {

    private val formatter = Formatter()

    init {
        imports.remove("java.util.*")
        imports.remove("java.time.*")
        import("org.springframework.boot.SpringApplication")
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
        "java",
        getPackage().replace(".", separator),
        this.getClassName() + ".java"
    )

    override fun generate(): String =
        formatter.formatSource(
            buildString {
                append("package ${getPackage()};")
                append(getImports())
                append("@SpringBootApplication\n")
                append("public class ${getClassName()} {")
                append("public static void main(String[] args) {")
                append("SpringApplication.run(${getClassName()}.class, args);")
                append("}")
                append("}")
            }
        )
}

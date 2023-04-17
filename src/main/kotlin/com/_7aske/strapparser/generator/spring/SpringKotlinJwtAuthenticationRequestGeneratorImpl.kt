package com._7aske.strapparser.generator.spring

import com._7aske.strapparser.generator.DataTypeResolver
import com._7aske.strapparser.generator.GeneratorContext
import com._7aske.strapparser.generator.kotlin.Formatter
import com._7aske.strapparser.generator.kotlin.KotlinClassGenerator
import java.nio.file.Path
import java.nio.file.Paths

class SpringKotlinJwtAuthenticationRequestGeneratorImpl(
    ctx: GeneratorContext,
    dataTypeResolver: DataTypeResolver
) : KotlinClassGenerator(
    ctx, dataTypeResolver
) {
    private val formatter = Formatter()

    override fun getOutputFilePath(): Path = Paths.get(
        ctx.getOutputLocation(),
        "src",
        "main",
        "kotlin",
        getPackage().replace(".", separator),
        this.getClassName() + ".kt"
    )

    override fun generate(): String = formatter.formatSource(
        buildString {
            appendLine("package ${getPackage()}")
            appendLine(getImports())

            appendLine("data class ${getClassName()}(")
            appendLine("var username: String,")
            appendLine("var password: String")
            appendLine(")")
        }
    )

    override fun getClassName(): String = "JwtAuthenticationRequest"

    override fun getPackage(): String = ctx.getPackageName("security")
}

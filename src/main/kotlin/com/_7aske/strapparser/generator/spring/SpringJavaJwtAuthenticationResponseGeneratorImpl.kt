package com._7aske.strapparser.generator.spring

import com._7aske.strapparser.extensions.uncapitalize
import com._7aske.strapparser.generator.java.JavaClassGenerator
import com._7aske.strapparser.generator.DataTypeResolver
import com._7aske.strapparser.generator.GeneratorContext
import com._7aske.strapparser.generator.java.Lombok
import com.google.googlejavaformat.java.Formatter
import java.nio.file.Path
import java.nio.file.Paths

class SpringJavaJwtAuthenticationResponseGeneratorImpl(
    ctx: GeneratorContext,
    dataTypeResolver: DataTypeResolver
) : JavaClassGenerator(
    ctx, dataTypeResolver
) {
    private val formatter = Formatter()

    init {
        if (ctx.args.lombok) {
            import(Lombok.PACKAGE + ".*")
        }
    }

    override fun getOutputFilePath(): Path = Paths.get(
        ctx.getOutputLocation(),
        "src",
        "main",
        "java",
        getPackage().replace(".", separator),
        this.getClassName() + ".java"
    )

    override fun generate(): String = formatter.formatSource(
        buildString {
            append("package ${getPackage()};")
            append(getImports())
            if (ctx.args.lombok) {
                append(Lombok.Data)
            }
            append("public final class ${getClassName()} {")
            append("private final String token;")
            if (!ctx.args.lombok) {
                append("public ${getClassName()}(String token) {")
                append("this.token = token;")
                append("}")
                append(
                    "public String getToken() {" +
                        "return token;" +
                        "}"
                )
            }
            append("}")
        }
    )

    override fun getVariableName(): String = getClassName().uncapitalize()

    override fun getClassName(): String = "JwtAuthenticationResponse"

    override fun getPackage(): String = ctx.getPackageName("security")

    override fun getFQCN(): String = getPackage() + "." + getClassName()
}

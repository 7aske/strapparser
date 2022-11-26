package com._7aske.strapparser.generator.spring

import com._7aske.strapparser.extensions.uncapitalize
import com._7aske.strapparser.generator.BaseGenerator
import com._7aske.strapparser.generator.DataTypeResolver
import com._7aske.strapparser.generator.GeneratorContext
import com._7aske.strapparser.generator.java.Lombok
import com.google.googlejavaformat.java.Formatter
import java.nio.file.Path
import java.nio.file.Paths

class SpringJavaJwtAuthenticationRequestGeneratorImpl(
    ctx: GeneratorContext,
    dataTypeResolver: DataTypeResolver
) : BaseGenerator(
    ctx, dataTypeResolver
) {
    private val formatter = Formatter()

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
            if (ctx.args.lombok) {
                append(Lombok.Data)
            }
            append("public final class ${getClassName()} {")
            append("private final String username;")
            append("private final String password;")
            if (!ctx.args.lombok) {
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

    override fun getClassName(): String = "JwtAuthenticationRequest"

    override fun getPackage(): String = ctx.getPackageName("security")

    override fun getFQCN(): String = getPackage() + "." + getClassName()
}

package com._7aske.strapparser.generator.spring

import com._7aske.strapparser.generator.DataTypeResolver
import com._7aske.strapparser.generator.GeneratorContext
import com._7aske.strapparser.generator.java.Formatter
import com._7aske.strapparser.generator.java.JavaClassGenerator
import com._7aske.strapparser.generator.java.Lombok
import java.nio.file.Path
import java.nio.file.Paths

class SpringJavaJwtAuthenticationRequestGeneratorImpl(
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
            append("private final String username;")
            append("private final String password;")
            if (!ctx.args.lombok) {
                append("public ${getClassName()}(String username, String password) {")
                append("this.username = username;")
                append("this.password = password;")
                append("}")
                append("public String getUsername() {")
                append("return username;")
                append("}")
                append("public String getPassword() {")
                append("return password;")
                append("}")
            }
            append("}")
        }
    )

    override fun getClassName(): String = "JwtAuthenticationRequest"

    override fun getPackage(): String = ctx.getPackageName("security")
}

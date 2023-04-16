package com._7aske.strapparser.generator.spring

import com._7aske.strapparser.extensions.capitalize
import com._7aske.strapparser.generator.DataTypeResolver
import com._7aske.strapparser.generator.GeneratorContext
import com._7aske.strapparser.generator.java.JavaClassGenerator
import com.google.googlejavaformat.java.Formatter
import java.nio.file.Path
import java.nio.file.Paths

class SpringJavaApiDocConfigGeneratorImpl(
    ctx: GeneratorContext,
    dataTypeResolver: DataTypeResolver
) : JavaClassGenerator(ctx, dataTypeResolver) {

    private val formatter = Formatter()

    init {
        import("org.springframework.context.annotation.Configuration")
        import("io.swagger.v3.oas.annotations.OpenAPIDefinition")
        import("io.swagger.v3.oas.annotations.info.Info")
        if (ctx.args.security) {
            import("io.swagger.v3.oas.annotations.security.SecurityScheme")
            import("io.swagger.v3.oas.annotations.enums.SecuritySchemeType")
        }

        imports.remove("java.util.*")
        imports.remove("java.time.*")
    }

    override fun getClassName(): String =
        "ApiDocConfig"

    override fun getPackage(): String =
        ctx.getPackageName("config")

    override fun getOutputFilePath(): Path =
        Paths.get(
            ctx.getOutputLocation(),
            "src",
            "main",
            "java",
            getPackage().replace(".", separator),
            getClassName() + ".java"
        )

    override fun generate(): String = formatter.formatSource(
        buildString {
            appendLine("package ${getPackage()};")
            appendLine()
            appendLine()
            appendLine(getImports())
            appendLine()
            appendLine("@Configuration")
            appendLine("@OpenAPIDefinition(")
            appendLine("    info = @Info(")
            appendLine("        title = \"${ctx.args.name.capitalize()} API\",")
            appendLine("        version = \"v1\",")
            appendLine("        description = \"${ctx.args.name.capitalize()} API documentation\"")
            appendLine("    )")
            appendLine(")")
            if (ctx.args.security) {
                appendLine("@SecurityScheme(")
                appendLine("    name = \"bearerAuth\",")
                appendLine("    type = SecuritySchemeType.HTTP,")
                appendLine("    scheme = \"bearer\")")
            }
            appendLine("public class ${getClassName()} {}")
        }
    )
}

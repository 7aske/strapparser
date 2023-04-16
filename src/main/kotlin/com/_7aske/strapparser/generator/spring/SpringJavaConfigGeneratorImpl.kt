package com._7aske.strapparser.generator.spring

import com._7aske.strapparser.generator.AvailableDatabases.*
import com._7aske.strapparser.generator.DataTypeResolver
import com._7aske.strapparser.generator.GeneratorContext
import com._7aske.strapparser.generator.java.JavaClassGenerator
import com.google.googlejavaformat.java.Formatter
import java.nio.file.Path
import java.nio.file.Paths

class SpringJavaConfigGeneratorImpl(
    ctx: GeneratorContext,
    dataTypeResolver: DataTypeResolver
) : JavaClassGenerator(ctx, dataTypeResolver) {

    private val formatter = Formatter()

    init {
        if (ctx.args.auditable) {
            import("org.springframework.data.domain.AuditorAware")
            import("org.springframework.context.annotation.Bean")
            import("org.springframework.security.core.Authentication")
            import("org.springframework.security.core.context.SecurityContextHolder")
            import("java.util.Optional")
            when (ctx.args.database) {
                POSTGRES, MARIADB, MYSQL -> import("org.springframework.data.jpa.repository.config.EnableJpaAuditing")
                MONGODB -> import("org.springframework.data.mongodb.config.EnableMongoAuditing")
            }
        }
        import("org.springframework.context.annotation.Configuration")
    }

    override fun getClassName(): String =
        "Config"

    override fun getPackage(): String =
        ctx.getPackageName("config")

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
            appendLine("package ${getPackage()};")
            appendLine(getImports())
            appendLine("@Configuration")
            if (ctx.args.auditable) {
                appendLine(getEnableAuditing())
            }
            appendLine("public class ${getClassName()} {")
            if (ctx.args.auditable) {
                appendLine("    ${getAuditorAware()}")
            }
            appendLine("}")
        }
    )

    private fun getEnableAuditing() =
        when (ctx.args.database) {
            POSTGRES, MARIADB, MYSQL -> "@EnableJpaAuditing"
            MONGODB -> "@EnableMongoAuditing"
        }

    private fun getAuditorAware(): String =
        buildString {
            appendLine("    @Bean")
            appendLine("    public AuditorAware<String> auditorProvider() {")
            appendLine("        return () -> {")
            appendLine(
                "            Authentication authentication = SecurityContextHolder.getContext()" +
                    ".getAuthentication();"
            )
            appendLine("            if (authentication == null || !authentication.isAuthenticated()) {")
            appendLine("                return Optional.empty();")
            appendLine("            }")
            appendLine("            return Optional.ofNullable(authentication.getName());")
            appendLine("        };")
            appendLine("    }")
        }
}

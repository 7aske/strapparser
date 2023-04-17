package com._7aske.strapparser.generator.spring

import com._7aske.strapparser.generator.AvailableDatabases.*
import com._7aske.strapparser.generator.DataTypeResolver
import com._7aske.strapparser.generator.GeneratorContext
import com._7aske.strapparser.generator.java.JavaClassGenerator
import com._7aske.strapparser.generator.kotlin.Formatter
import java.nio.file.Path
import java.nio.file.Paths

class SpringKotlinConfigGeneratorImpl(
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
        "kotlin",
        getPackage().replace(".", separator),
        this.getClassName() + ".kt"
    )

    override fun generate(): String = formatter.formatSource(
        buildString {
            appendLine("package ${getPackage()}")
            appendLine(getImports())
            appendLine("@Configuration")
            if (ctx.args.auditable) {
                appendLine(getEnableAuditing())
            }
            appendLine("open class ${getClassName()}")
            if (ctx.args.auditable) {
                appendLine("    {${getAuditorAware()}}")
            }
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
            appendLine("    open fun auditorAware(): AuditorAware<String> = AuditorAware<String> {")
            appendLine("        (SecurityContextHolder.getContext().authentication?.name ?: \"system\")")
            appendLine("            .let { Optional.of(it)}")
            appendLine("    }")
        }
}

package com._7aske.strapparser.generator.spring

import com._7aske.strapparser.generator.DataTypeResolver
import com._7aske.strapparser.generator.EntityGenerator
import com._7aske.strapparser.generator.GeneratorContext
import com._7aske.strapparser.generator.kotlin.Formatter
import com._7aske.strapparser.generator.kotlin.KotlinClassGenerator
import com._7aske.strapparser.generator.spring.SpringPackages.SPRING_DOMAIN_PACKAGE
import java.nio.file.Path
import java.nio.file.Paths

class SpringKotlinServiceGeneratorImpl(
    internal val entity: EntityGenerator,
    ctx: GeneratorContext,
    dataTypeResolver: DataTypeResolver
) : KotlinClassGenerator(
    ctx, dataTypeResolver
) {
    private val formatter = Formatter()

    init {
        imports.remove("java.util.*")
        imports.remove("java.time.*")
        import("$SPRING_DOMAIN_PACKAGE.Page")
        import("$SPRING_DOMAIN_PACKAGE.Pageable")
        import(entity.getFQCN())
        if (entity.entity.isUserDetails() && ctx.args.security) {
            import("org.springframework.security.core.userdetails.UserDetailsService")
        }
    }

    override fun getOutputFilePath(): Path =
        Paths.get(
            ctx.getOutputLocation(),
            "src",
            "main",
            "kotlin",
            getPackage().replace(".", separator),
            this.getClassName() + ".kt"
        )

    override fun generate(): String {
        val extends = mutableListOf<String>()
        if (entity.entity.isUserDetails() && ctx.args.security) {
            extends.add("UserDetailsService")
        }

        return formatter.formatSource(
            buildString {
                appendLine("package ${getPackage()}")
                appendLine(getImports())
                append("interface ${getClassName()}")
                if (extends.isNotEmpty()) {
                    append(" : ")
                    append(extends.joinToString())
                }
                appendLine(" {")
                appendLine("fun findAll(page: Pageable): Page<${entity.getClassName()}> ")
                appendLine("fun findById(${entity.getIdFieldsAsArguments()}): ${entity.getClassName()}")
                appendLine(
                    "fun save(${entity.getVariableName()}: ${entity.getClassName()}): ${
                    entity.getClassName()}"
                )
                appendLine(
                    "fun update(${entity.getVariableName()}: ${entity.getClassName()}): ${
                    entity.getClassName()} "
                )
                appendLine("fun deleteById(${entity.getIdFieldsAsArguments()})")
                append("}")
            }
        )
    }

    override fun getClassName(): String =
        entity.getClassName() + "Service"

    override fun getPackage(): String =
        ctx.getPackageName("service")
}

package com._7aske.strapparser.generator.spring

import com._7aske.strapparser.generator.DataTypeResolver
import com._7aske.strapparser.generator.EntityGenerator
import com._7aske.strapparser.generator.GeneratorContext
import com._7aske.strapparser.generator.ServiceGenerator
import com._7aske.strapparser.generator.spring.SpringJavaPackages.SPRING_DOMAIN_PACKAGE
import com.google.googlejavaformat.java.Formatter
import java.nio.file.Path
import java.nio.file.Paths

class SpringJavaServiceGeneratorImpl(
    entity: EntityGenerator,
    ctx: GeneratorContext,
    dataTypeResolver: DataTypeResolver
) : ServiceGenerator(
    entity, ctx, dataTypeResolver
) {
    private val formatter = Formatter()

    init {
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
            "java",
            getPackage().replace(".", separator),
            this.getClassName() + ".java"
        )

    override fun generate(): String {
        val extends = mutableListOf<String>()
        if (entity.entity.isUserDetails() && ctx.args.security) {
            extends.add("UserDetailsService")
        }

        return formatter.formatSource(
            buildString {
                append("package ${getPackage()};")
                append(getImports())
                append("public interface ${getClassName()}")
                if (extends.isNotEmpty()) {
                    append(" extends ")
                    append(extends.joinToString())
                }
                append(" {")
                append("Page<${entity.getClassName()}> ")
                append("findAll(Pageable page);")
                append("${entity.getClassName()} findById(${entity.getIdFieldsAsArguments()});")
                append("${entity.getClassName()} save(${entity.getClassName()} ${entity.getVariableName()});")
                append("${entity.getClassName()} update(${entity.getClassName()} ${entity.getVariableName()});")
                append("void deleteById(${entity.getIdFieldsAsArguments()});")
                append("}")
            }
        )
    }

    override fun getClassName(): String =
        entity.getClassName() + "Service"

    override fun getPackage(): String =
        ctx.getPackageName("service")
}

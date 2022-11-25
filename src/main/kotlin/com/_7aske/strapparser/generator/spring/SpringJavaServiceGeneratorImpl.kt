package com._7aske.strapparser.generator.spring

import com._7aske.strapparser.extensions.uncapitalize
import com._7aske.strapparser.generator.*
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

    override fun getOutputFilePath(): Path =
        Paths.get(
            ctx.getOutputLocation(),
            "src",
            "main",
            "java",
            resolvePackage().replace(".", separator),
            this.resolveClassName() + ".java"
        )

    override fun generate(): String =
        formatter.formatSource(
            buildString {
                append("package ${resolvePackage()};")
                append("public interface ${resolveClassName()} {")
                append("$SPRING_DOMAIN_PACKAGE.Page<${entity.resolveFQCN()}> ")
                append("findAll($SPRING_DOMAIN_PACKAGE.Pageable page);")
                append("${entity.resolveFQCN()} findById(${entity.resolveIdFieldsParameters()});")
                append("${entity.resolveFQCN()} save(${entity.resolveFQCN()} ${entity.resolveVariableName()});")
                append("${entity.resolveFQCN()} update(${entity.resolveFQCN()} ${entity.resolveVariableName()});")
                append("void deleteById(${entity.resolveIdFieldsParameters()});")
                append("}")
            }
        )

    override fun resolveVariableName(): String =
        resolveClassName().uncapitalize()

    override fun resolveClassName(): String =
        entity.resolveClassName() + "Service"

    override fun resolvePackage(): String =
        ctx.getPackageName("service")

    override fun resolveFQCN(): String =
        resolvePackage() + "." + resolveClassName()
}

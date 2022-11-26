package com._7aske.strapparser.generator.spring

import com._7aske.strapparser.extensions.uncapitalize
import com._7aske.strapparser.generator.DataTypeResolver
import com._7aske.strapparser.generator.EntityGenerator
import com._7aske.strapparser.generator.GeneratorContext
import com._7aske.strapparser.generator.ServiceGenerator
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
            getPackage().replace(".", separator),
            this.getClassName() + ".java"
        )

    override fun generate(): String =
        formatter.formatSource(
            buildString {
                append("package ${getPackage()};")
                append("public interface ${getClassName()} {")
                append("$SPRING_DOMAIN_PACKAGE.Page<${entity.getFQCN()}> ")
                append("findAll($SPRING_DOMAIN_PACKAGE.Pageable page);")
                append("${entity.getFQCN()} findById(${entity.getIdFieldsAsArguments()});")
                append("${entity.getFQCN()} save(${entity.getFQCN()} ${entity.getVariableName()});")
                append("${entity.getFQCN()} update(${entity.getFQCN()} ${entity.getVariableName()});")
                append("void deleteById(${entity.getIdFieldsAsArguments()});")
                append("}")
            }
        )

    override fun getVariableName(): String =
        getClassName().uncapitalize()

    override fun getClassName(): String =
        entity.getClassName() + "Service"

    override fun getPackage(): String =
        ctx.getPackageName("service")

    override fun getFQCN(): String =
        getPackage() + "." + getClassName()
}

package com._7aske.strapparser.generator.spring

import com._7aske.strapparser.extensions.uncapitalize
import com._7aske.strapparser.generator.*
import com.google.googlejavaformat.java.Formatter
import java.nio.file.Path
import java.nio.file.Paths

class SpringJavaServiceImplGeneratorImpl(
    private val repository: RepositoryGenerator,
    private val service: ServiceGenerator,
    entity: EntityGenerator,
    ctx: GeneratorContext,
    dataTypeResolver: DataTypeResolver
) : ServiceImplGenerator(
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
                append("@org.springframework.stereotype.Service\n")
                append("public class ${resolveClassName()} implements ")
                append(service.resolveFQCN()).append(" {")
                append("private final ${repository.resolveFQCN()} ${repository.resolveVariableName()};")
                append("public ").append(resolveClassName()).append("(")
                append(repository.resolveFQCN()).append(" ")
                    .append(repository.resolveVariableName())
                append(") {")
                append("this.${repository.resolveVariableName()} = ${repository.resolveVariableName()};")
                append("}")
                append(generateBody())
                append("}")
            }
        )

    private fun generateBody(): String =
        buildString {
            append(generateReadMethods())
            append(generateCreateMethods())
            append(generateUpdateMethods())
            append(generateDeleteMethods())
        }

    private fun generateDeleteMethods(): String =
        buildString {
            append("public void ")
            append("deleteById(${entity.resolveIdFieldsParameters()}) {")
            append("${repository.resolveVariableName()}.deleteById(${entity.resolveIdFieldVariables()});")
            append("}")
        }

    private fun generateUpdateMethods(): String =
        buildString {
            append("public ").append(entity.resolveFQCN()).append(" ")
            append("update(${entity.resolveFQCN()} ${entity.resolveVariableName()}) {")
            append("return ${repository.resolveVariableName()}.save(${entity.resolveVariableName()});")
            append("}")
        }

    private fun generateCreateMethods(): String =
        buildString {
            append("public ").append(entity.resolveFQCN()).append(" ")
            append("save(${entity.resolveFQCN()} ${entity.resolveVariableName()}) {")
            append("return ${repository.resolveVariableName()}.save(${entity.resolveVariableName()});")
            append("}")
        }

    private fun generateReadMethods(): String =
        buildString {
            append("public ").append("$SPRING_DOMAIN_PACKAGE.Page<${entity.resolveFQCN()}>").append(" ")
            append("findAll($SPRING_DOMAIN_PACKAGE.Pageable page) {")
            append("return ${repository.resolveVariableName()}.findAll(page);")
            append("}")

            append("public ").append(entity.resolveFQCN()).append(" ")
            append("findById(${entity.resolveIdFieldsParameters()}) {")
            append(
                "return ${repository.resolveVariableName()}.findById(${entity.resolveIdFieldVariables()})" +
                    ".orElseThrow(() -> " +
                    "new java.util.NoSuchElementException(\"${entity.resolveClassName()} not found\"));"
            )
            append("}")
        }

    override fun resolveVariableName(): String =
        resolveClassName().uncapitalize()

    override fun resolveClassName(): String =
        entity.resolveClassName() + "ServiceImpl"

    override fun resolvePackage(): String =
        ctx.getPackageName("service", "impl")

    override fun resolveFQCN(): String =
        resolvePackage() + "." + resolveClassName()
}

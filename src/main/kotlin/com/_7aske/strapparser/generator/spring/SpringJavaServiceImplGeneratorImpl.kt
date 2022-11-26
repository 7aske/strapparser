package com._7aske.strapparser.generator.spring

import com._7aske.strapparser.extensions.uncapitalize
import com._7aske.strapparser.generator.*
import com._7aske.strapparser.generator.java.Lombok
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
            getPackage().replace(".", separator),
            this.getClassName() + ".java"
        )

    override fun generate(): String =
        formatter.formatSource(
            buildString {
                append("package ${getPackage()};")
                append("@org.springframework.stereotype.Service\n")
                if (ctx.args.lombok) {
                    append(Lombok.RequiredArgsConstructor)
                }
                append("public class ${getClassName()} implements ")
                append(service.getFQCN()).append(" {")
                append("private final ${repository.getFQCN()} ${repository.getVariableName()};")

                if (!ctx.args.lombok) {
                    append("public ").append(getClassName()).append("(")
                    append(repository.getFQCN()).append(" ")
                        .append(repository.getVariableName())
                    append(") {")
                    append("this.${repository.getVariableName()} = ${repository.getVariableName()};")
                    append("}")
                }

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
            append("deleteById(${entity.getIdFieldsAsArguments()}) {")
            append("${repository.getVariableName()}.deleteById(${entity.getCompositeIdFieldVariables()});")
            append("}")
        }

    private fun generateUpdateMethods(): String =
        buildString {
            append("public ").append(entity.getFQCN()).append(" ")
            append("update(${entity.getFQCN()} ${entity.getVariableName()}) {")
            append("return ${repository.getVariableName()}.save(${entity.getVariableName()});")
            append("}")
        }

    private fun generateCreateMethods(): String =
        buildString {
            append("public ").append(entity.getFQCN()).append(" ")
            append("save(${entity.getFQCN()} ${entity.getVariableName()}) {")
            append("return ${repository.getVariableName()}.save(${entity.getVariableName()});")
            append("}")
        }

    private fun generateReadMethods(): String =
        buildString {
            append("public ").append("$SPRING_DOMAIN_PACKAGE.Page<${entity.getFQCN()}>")
                .append(" ")
            append("findAll($SPRING_DOMAIN_PACKAGE.Pageable page) {")
            append("return ${repository.getVariableName()}.findAll(page);")
            append("}")

            append("public ").append(entity.getFQCN()).append(" ")
            append("findById(${entity.getIdFieldsAsArguments()}) {")
            append(
                "return ${repository.getVariableName()}.findById(${entity.getCompositeIdFieldVariables()})" +
                    ".orElseThrow(() -> " +
                    "new java.util.NoSuchElementException(\"${entity.getClassName()} not found\"));"
            )
            append("}")
        }

    override fun getVariableName(): String =
        getClassName().uncapitalize()

    override fun getClassName(): String =
        entity.getClassName() + "ServiceImpl"

    override fun getPackage(): String =
        ctx.getPackageName("service", "impl")

    override fun getFQCN(): String =
        getPackage() + "." + getClassName()
}

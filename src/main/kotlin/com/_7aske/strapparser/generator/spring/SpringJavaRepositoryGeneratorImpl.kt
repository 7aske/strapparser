package com._7aske.strapparser.generator.spring

import com._7aske.strapparser.generator.DataTypeResolver
import com._7aske.strapparser.generator.EntityGenerator
import com._7aske.strapparser.generator.GeneratorContext
import com._7aske.strapparser.generator.RepositoryGenerator
import com.google.googlejavaformat.java.Formatter
import java.nio.file.Path
import java.nio.file.Paths

class SpringJavaRepositoryGeneratorImpl(
    entity: EntityGenerator,
    ctx: GeneratorContext,
    dataTypeResolver: DataTypeResolver
) : RepositoryGenerator(entity, ctx, dataTypeResolver) {
    private val formatter = Formatter()

    override fun generate(): String {
        return formatter.formatSource(buildString {
            append("package ${ctx.getPackageName("repository")};")

            append("public interface ").append(resolveClassName())
            append(" extends ")
            append(
                "org.springframework.data.jpa.repository.JpaRepository<${
                    dataTypeResolver.resolveDataType(entity.resolveClassName())
                }, ${
                    dataTypeResolver.resolveDataType(entity.getIdFields()[0])
                }>"
            )

            if (ctx.args.specification) {
                append(", ")
                append(
                    "org.springframework.data.jpa.repository.JpaSpecificationExecutor<${
                        dataTypeResolver.resolveDataType(
                            entity.resolveClassName()
                        )
                    }>"
                )
            }
            append("{}")
            println(this.toString())
        })
    }

    override fun resolveClassName(): String =
        entity.resolveClassName() + "Repository"

    override fun getOutputFilePath(): Path = Paths.get(
        ctx.getOutputLocation(),
        "src",
        "main",
        "java",
        ctx.getPackageName().replace(".", separator),
        "repository",
        this.resolveClassName() + ".java"
    )

}
package com._7aske.strapparser.generator.spring

import com._7aske.strapparser.extensions.uncapitalize
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
        return formatter.formatSource(
            buildString {
                append("package ${getPackage()};")

                append("public interface ").append(getClassName())
                append(" extends ")
                append(
                    "org.springframework.data.jpa.repository.JpaRepository<${
                    dataTypeResolver.resolveDataType(entity.getFQCN())
                    }, ${
                    entity.getIdFQCN()
                    }>"
                )

                if (ctx.args.specification) {
                    append(", ")
                    append(
                        "org.springframework.data.jpa.repository.JpaSpecificationExecutor<${
                        dataTypeResolver.resolveDataType(
                            entity.getClassName()
                        )
                        }>"
                    )
                }
                append("{}")
            }
        )
    }

    override fun getVariableName(): String =
        getClassName().uncapitalize()

    override fun getClassName(): String =
        entity.getClassName() + "Repository"

    override fun getPackage(): String =
        ctx.getPackageName("repository")

    override fun getFQCN(): String =
        getPackage() + "." + getClassName()

    override fun getOutputFilePath(): Path = Paths.get(
        ctx.getOutputLocation(),
        "src",
        "main",
        "java",
        getPackage().replace(".", separator),
        this.getClassName() + ".java"
    )
}

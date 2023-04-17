package com._7aske.strapparser.generator.spring

import com._7aske.strapparser.extensions.capitalize
import com._7aske.strapparser.generator.DataTypeResolver
import com._7aske.strapparser.generator.EntityGenerator
import com._7aske.strapparser.generator.GeneratorContext
import com._7aske.strapparser.generator.RepositoryGenerator
import com._7aske.strapparser.generator.kotlin.Formatter
import java.nio.file.Path
import java.nio.file.Paths

class SpringKotlinRepositoryGeneratorImpl(
    entity: EntityGenerator,
    ctx: GeneratorContext,
    dataTypeResolver: DataTypeResolver
) : RepositoryGenerator(entity, ctx, dataTypeResolver) {
    private val formatter = Formatter()

    init {
        imports.remove("java.util.*")
        imports.remove("java.time.*")
        import(entity.getFQCN())
        import("org.springframework.data.jpa.repository.JpaRepository")
        if (ctx.args.specification) {
            import("org.springframework.data.jpa.repository.JpaSpecificationExecutor")
        }
    }

    override fun generate(): String {
        return formatter.formatSource(
            buildString {
                appendLine("package ${getPackage()}")
                appendLine(getImports())

                append("interface ").append(getClassName())
                append(" : ")
                append(
                    "JpaRepository<${
                    dataTypeResolver.resolveDataType(entity.getClassName())
                    }, ${
                    entity.getIdFQCN()
                    }>"
                )

                if (ctx.args.specification) {
                    append(", ")
                    append(
                        "JpaSpecificationExecutor<${
                        dataTypeResolver.resolveDataType(
                            entity.getClassName()
                        )
                        }>"
                    )
                }
                if (entity.entity.isUserDetails() && ctx.args.security) {
                    append("{")
                    val usernameField = entity.entity.getUsernameField()
                    if (usernameField != null) {
                        append(
                            "fun findBy${usernameField.name.capitalize()}(username: String) : ${
                            entity.getClassName()}?  "
                        )
                    }
                    append("}")
                }
            }
        )
    }

    override fun getClassName(): String =
        entity.getClassName() + "Repository"

    override fun getPackage(): String =
        ctx.getPackageName("repository")

    override fun getOutputFilePath(): Path = Paths.get(
        ctx.getOutputLocation(),
        "src",
        "main",
        "kotlin",
        getPackage().replace(".", separator),
        this.getClassName() + ".kt"
    )
}

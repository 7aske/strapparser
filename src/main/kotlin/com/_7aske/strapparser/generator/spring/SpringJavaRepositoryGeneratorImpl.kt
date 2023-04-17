package com._7aske.strapparser.generator.spring

import com._7aske.strapparser.extensions.capitalize
import com._7aske.strapparser.generator.DataTypeResolver
import com._7aske.strapparser.generator.EntityGenerator
import com._7aske.strapparser.generator.GeneratorContext
import com._7aske.strapparser.generator.RepositoryGenerator
import com._7aske.strapparser.generator.java.Formatter
import java.nio.file.Path
import java.nio.file.Paths

class SpringJavaRepositoryGeneratorImpl(
    entity: EntityGenerator,
    ctx: GeneratorContext,
    dataTypeResolver: DataTypeResolver
) : RepositoryGenerator(entity, ctx, dataTypeResolver) {
    private val formatter = Formatter()

    init {
        import(entity.getFQCN())
        import("org.springframework.data.jpa.repository.JpaRepository")
        if (ctx.args.specification) {
            import("org.springframework.data.jpa.repository.JpaSpecificationExecutor")
        }
    }

    override fun generate(): String {
        return formatter.formatSource(
            buildString {
                append("package ${getPackage()};")
                append(getImports())

                append("public interface ").append(getClassName())
                append(" extends ")
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
                append("{")
                if (entity.entity.isUserDetails() && ctx.args.security) {
                    val usernameField = entity.entity.getUsernameField()
                    if (usernameField != null) {
                        append(
                            "Optional<${entity.getClassName()}> " +
                                "findBy${usernameField.name.capitalize()}(String username);"
                        )
                    }
                }
                append("}")
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
        "java",
        getPackage().replace(".", separator),
        this.getClassName() + ".java"
    )
}

package com._7aske.strapparser.generator.spring

import com._7aske.strapparser.extensions.capitalize
import com._7aske.strapparser.generator.DataTypeResolver
import com._7aske.strapparser.generator.EntityGenerator
import com._7aske.strapparser.generator.GeneratorContext
import com._7aske.strapparser.generator.RepositoryGenerator
import com._7aske.strapparser.generator.kotlin.Formatter
import com._7aske.strapparser.generator.kotlin.KotlinClassGenerator
import com._7aske.strapparser.generator.kotlin.KotlinMethodBuilder
import com._7aske.strapparser.generator.spring.SpringPackages.SPRING_DOMAIN_PACKAGE
import java.nio.file.Path
import java.nio.file.Paths

class SpringKotlinServiceImplGeneratorImpl(
    private val repository: RepositoryGenerator,
    private val service: SpringKotlinServiceGeneratorImpl,
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
        import("org.springframework.stereotype.Service")
        import("$SPRING_DOMAIN_PACKAGE.Page")
        import("$SPRING_DOMAIN_PACKAGE.Pageable")
        import(entity.getFQCN())
        import(service.getFQCN())
        import(repository.getFQCN())
        if (entity.entity.isUserDetails() && ctx.args.security) {
            import("org.springframework.security.core.userdetails.UserDetails")
            import("org.springframework.security.core.userdetails.UsernameNotFoundException")
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

    override fun generate(): String =
        formatter.formatSource(
            buildString {
                append("package ${getPackage()};")
                append(getImports())
                append("@Service\n")
                append("open class ${getClassName()}")
                appendLine("( ")
                appendLine("private val ${repository.getVariableName()}: ${repository.getClassName()}")
                append(" )")
                append(": ")
                append(service.getClassName())
                append(" {")
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
            if (entity.entity.isUserDetails() && ctx.args.security) {
                append(generateUserDetailsLoadByUsername())
            }
        }

    private fun generateUserDetailsLoadByUsername(): String =
        KotlinMethodBuilder.of("loadUserByUsername").apply {
            override = true
            returnType =
                "UserDetails"
            parameters.add(listOf("String", "username"))
            val usernameField = entity.entity.getUsernameField()
            implementation = if (usernameField == null) {
                "TODO(\"Not yet implemented\")"
            } else {
                """
                return ${repository.getVariableName()}.findBy${entity.entity.getUsernameField()?.name?.capitalize()}(username)
                    ?: throw UsernameNotFoundException(String.format("User with username %s not found", username))
                """.trimIndent()
            }
        }.build()

    private fun generateDeleteMethods(): String =
        KotlinMethodBuilder.of("deleteById").apply {
            override = true
            parameters.addAll(
                entity.getIdFields().map {
                    listOf(
                        dataTypeResolver.resolveDataType(it), it.name
                    )
                }
            )
            implementation =
                "${repository.getVariableName()}.deleteById(${entity.getCompositeIdFieldVariables()})"
        }.build()

    private fun generateUpdateMethods(): String =
        KotlinMethodBuilder.of("update").apply {
            override = true
            parameters.add(
                listOf(
                    entity.getClassName(), entity.getVariableName()
                )
            )
            returnType = entity.getClassName()
            implementation =
                "return ${repository.getVariableName()}.save(${entity.getVariableName()})"
        }.build()

    private fun generateCreateMethods(): String =
        KotlinMethodBuilder.of("save").apply {
            override = true
            parameters.add(
                listOf(
                    entity.getClassName(), entity.getVariableName()
                )
            )
            returnType = entity.getClassName()
            implementation =
                "return ${repository.getVariableName()}.save(${entity.getVariableName()})"
        }.build()

    private fun generateReadMethods(): String =
        buildString {
            append(
                KotlinMethodBuilder.of("findAll").apply {
                    override = true
                    parameters.add(
                        listOf(
                            "Pageable",
                            "page"
                        )
                    )
                    returnType =
                        "Page<${entity.getClassName()}>"
                    implementation =
                        "return ${repository.getVariableName()}.findAll(page)"
                }
            )
            append(
                KotlinMethodBuilder.of("findById").apply {
                    override = true
                    parameters.addAll(
                        entity.getIdFields().map {
                            listOf(
                                dataTypeResolver.resolveDataType(it), it.name
                            )
                        }
                    )
                    returnType = entity.getClassName()
                    implementation =
                        "return ${repository.getVariableName()}.findById(${entity.getCompositeIdFieldVariables()})" +
                        ".orElseThrow { " +
                        "NoSuchElementException(\"${entity.getClassName()} not found\") }"
                }
            )
        }

    override fun getClassName(): String =
        entity.getClassName() + "ServiceImpl"

    override fun getPackage(): String =
        ctx.getPackageName("service", "impl")
}

package com._7aske.strapparser.generator.spring

import com._7aske.strapparser.extensions.capitalize
import com._7aske.strapparser.extensions.uncapitalize
import com._7aske.strapparser.generator.*
import com._7aske.strapparser.generator.java.JavaMethodBuilder
import com._7aske.strapparser.generator.java.Lombok
import com._7aske.strapparser.generator.java.OVERRIDE
import com._7aske.strapparser.generator.spring.SpringJavaPackages.SPRING_DOMAIN_PACKAGE
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

    init {
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

        if (ctx.args.lombok) {
            import(Lombok.PACKAGE + ".*")
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

    override fun generate(): String =
        formatter.formatSource(
            buildString {
                append("package ${getPackage()};")
                append(getImports())
                append("@Service\n")
                if (ctx.args.lombok) {
                    append(Lombok.RequiredArgsConstructor)
                }
                append("public class ${getClassName()} implements ")
                append(service.getClassName()).append(" {")
                append("private final ${repository.getClassName()} ${repository.getVariableName()};")

                if (!ctx.args.lombok) {
                    append("public ").append(getClassName()).append("(")
                    append(repository.getClassName()).append(" ")
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
            if (entity.entity.isUserDetails() && ctx.args.security) {
                append(generateUserDetailsLoadByUsername())
            }
        }

    private fun generateUserDetailsLoadByUsername(): String =
        JavaMethodBuilder.of("loadUserByUsername").apply {
            annotations.add(OVERRIDE)
            returnType =
                "UserDetails"
            throws.add("UsernameNotFoundException")
            parameters.add(listOf("String", "username"))
            val usernameField = entity.entity.getUsernameField()
            implementation = if (usernameField == null) {
                "throw new IllegalStateException(\"Not yet implemented\");"
            } else {
                """
                return ${repository.getVariableName()}.findBy${entity.entity.getUsernameField()?.name?.capitalize()}(username)
                    .orElseThrow(() -> new UsernameNotFoundException(String.format("User with username %s not found", username)));
                """.trimIndent()
            }
        }.build()

    private fun generateDeleteMethods(): String =
        JavaMethodBuilder.of("deleteById").apply {
            annotations.add(OVERRIDE)
            parameters.addAll(
                entity.getIdFields().map {
                    listOf(
                        dataTypeResolver.resolveDataType(it), it.name
                    )
                }
            )
            implementation =
                "${repository.getVariableName()}.deleteById(${entity.getCompositeIdFieldVariables()});"
        }.build()

    private fun generateUpdateMethods(): String =
        JavaMethodBuilder.of("update").apply {
            annotations.add(OVERRIDE)
            parameters.add(
                listOf(
                    entity.getClassName(), entity.getVariableName()
                )
            )
            returnType = entity.getClassName()
            implementation =
                "return ${repository.getVariableName()}.save(${entity.getVariableName()});"
        }.build()

    private fun generateCreateMethods(): String =
        JavaMethodBuilder.of("save").apply {
            annotations.add(OVERRIDE)
            parameters.add(
                listOf(
                    entity.getClassName(), entity.getVariableName()
                )
            )
            returnType = entity.getClassName()
            implementation =
                "return ${repository.getVariableName()}.save(${entity.getVariableName()});"
        }.build()

    private fun generateReadMethods(): String =
        buildString {
            append(
                JavaMethodBuilder.of("findAll").apply {
                    annotations.add(OVERRIDE)
                    parameters.add(
                        listOf(
                            "Pageable",
                            "page"
                        )
                    )
                    returnType =
                        "Page<${entity.getClassName()}>"
                    implementation =
                        "return ${repository.getVariableName()}.findAll(page);"
                }
            )
            append(
                JavaMethodBuilder.of("findById").apply {
                    annotations.add(OVERRIDE)
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
                        ".orElseThrow(() -> " +
                        "new NoSuchElementException(\"${entity.getClassName()} not found\"));"
                }
            )
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

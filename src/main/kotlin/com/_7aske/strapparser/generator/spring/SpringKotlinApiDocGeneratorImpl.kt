package com._7aske.strapparser.generator.spring

import com._7aske.strapparser.extensions.plural
import com._7aske.strapparser.generator.DataTypeResolver
import com._7aske.strapparser.generator.GeneratorContext
import com._7aske.strapparser.generator.kotlin.Formatter
import com._7aske.strapparser.generator.kotlin.KotlinClassGenerator
import com._7aske.strapparser.generator.kotlin.KotlinMethodBuilder
import com._7aske.strapparser.generator.spring.SpringAnnotations.SECURITY_REQ
import java.nio.file.Path
import java.nio.file.Paths

class SpringKotlinApiDocGeneratorImpl(
    private val controllerGenerator: SpringControllerGenerator,
    ctx: GeneratorContext,
    dataTypeResolver: DataTypeResolver
) : KotlinClassGenerator(ctx, dataTypeResolver) {
    private val entityClassName =
        controllerGenerator.getEntityGenerator().getClassName()
    private val entityVariableName =
        controllerGenerator.getEntityGenerator().getVariableName()

    private val formatter = Formatter()

    init {
        import("${SpringPackages.OPENAPI_PACKAGE}.Operation")
        import("${SpringPackages.OPENAPI_PACKAGE}.Parameter")
        import("${SpringPackages.OPENAPI_PACKAGE}.tags.Tag")
        import("${SpringPackages.SPRING_BIND_PACKAGE}.*")
        import("${SpringPackages.SPRING_DOMAIN_PACKAGE}.Page")
        import("${SpringPackages.SPRING_DOMAIN_PACKAGE}.Pageable")
        import("${SpringPackages.SPRING_HTTP_PACKAGE}.ResponseEntity")
        import("${SpringPackages.SPRINGDOC_PACKAGE}.ParameterObject")
        import(controllerGenerator.getEntityGenerator().getFQCN())
        if (ctx.args.security) {
            import("io.swagger.v3.oas.annotations.security.SecurityRequirement")
        }
    }

    override fun getClassName(): String =
        "${entityClassName}Api"

    override fun getPackage(): String =
        ctx.getPackageName("api")

    override fun getOutputFilePath(): Path = Paths.get(
        ctx.getOutputLocation(),
        "src",
        "main",
        "kotlin",
        getPackage().replace(".", separator),
        this.getClassName() + ".kt"
    )

    override fun generate(): String = formatter.formatSource(
        buildString {
            appendLine("package ${getPackage()}")
            appendLine(getImports())
            appendLine("@Tag(name = \"${entityVariableName}\", description = \"$entityClassName API\")")
            appendLine("interface ").append(getClassName()).append(" {")
            appendLine(generateGetEndpoints())
            appendLine(generatePostEndpoints())
            appendLine(generatePutEndpoints())
            appendLine(generateDeleteEndpoints())
            append("}")
        }
    )

    private fun generateGetEndpoints(): String = buildString {
        appendLine(
            KotlinMethodBuilder.abstract("getAll${entityClassName.plural()}")
                .apply {
                    annotations.add(
                        "@Operation(tags = [\"${entityVariableName}\"]," +
                            " summary = \"Get all ${entityVariableName.plural()}\"," +
                            " description = \"Get all ${entityVariableName.plural()} with pagination\")"
                    )
                    if (ctx.args.security) {
                        annotations.add(SECURITY_REQ)
                    }
                    returnType =
                        "ResponseEntity<Page<$entityClassName>>"
                    parameters.add(
                        listOf(
                            "@ParameterObject",
                            "Pageable",
                            "page"
                        )
                    )
                }
        )

        appendLine(
            KotlinMethodBuilder.abstract("get${entityClassName}ById").apply {
                annotations.add(
                    "@Operation(tags = [\"${entityVariableName}\"]," +
                        " summary = \"Get ${entityVariableName}\"," +
                        " description = \"Get $entityVariableName by id\")"
                )
                if (ctx.args.security) {
                    annotations.add(SECURITY_REQ)
                }
                returnType = "ResponseEntity<$entityClassName>"
                parameters.addAll(
                    controllerGenerator.resolveIdFieldsParameters().map {
                        it.toMutableList().apply {
                            add(
                                0,
                                "@Parameter(description = \"$entityClassName id\")"
                            )
                        }
                    }
                )
            }
        )
    }

    private fun generatePostEndpoints(): String = buildString {
        appendLine(
            KotlinMethodBuilder.abstract("save$entityClassName").apply {
                annotations.add(
                    "@Operation(tags = [\"${entityVariableName}\"]," +
                        " summary = \"Save ${entityVariableName}\"," +
                        " description = \"Save ${entityVariableName}\")"
                )
                if (ctx.args.security) {
                    annotations.add(SECURITY_REQ)
                }
                returnType = "ResponseEntity<$entityClassName>"
                parameters.add(
                    listOf(
                        "@RequestBody",
                        entityClassName,
                        entityVariableName,
                    )
                )
            }
        )
    }

    private fun generatePutEndpoints(): String = buildString {
        appendLine(
            KotlinMethodBuilder.abstract("update$entityClassName").apply {
                annotations.add(
                    "@Operation(tags = [\"${entityVariableName}\"]," +
                        " summary = \"Update ${entityVariableName}\"," +
                        " description = \"Update ${entityVariableName}\")"
                )
                if (ctx.args.security) {
                    annotations.add(SECURITY_REQ)
                }
                returnType = "ResponseEntity<$entityClassName>"
                parameters.add(
                    listOf(
                        "@RequestBody",
                        entityClassName,
                        entityVariableName,
                    )
                )
            }
        )
    }

    private fun generateDeleteEndpoints(): String = buildString {

        appendLine(
            KotlinMethodBuilder.abstract("delete${entityClassName}ById").apply {
                annotations.add(
                    "@Operation(tags = [\"${entityVariableName}\"]," +
                        " summary = \"Delete ${entityVariableName}\"," +
                        " description = \"Delete $entityVariableName by id\")"
                )
                if (ctx.args.security) {
                    annotations.add(SECURITY_REQ)
                }
                returnType = "ResponseEntity<Void>"
                parameters.addAll(
                    controllerGenerator.resolveIdFieldsParameters().map {
                        it.toMutableList().apply {
                            add(
                                0,
                                "@Parameter(description = \"$entityClassName id\")"
                            )
                        }
                    }
                )
            }
        )
    }
}

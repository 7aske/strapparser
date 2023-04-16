package com._7aske.strapparser.generator.spring

import com._7aske.strapparser.extensions.plural
import com._7aske.strapparser.generator.ControllerGenerator
import com._7aske.strapparser.generator.DataTypeResolver
import com._7aske.strapparser.generator.GeneratorContext
import com._7aske.strapparser.generator.java.JavaClassGenerator
import com._7aske.strapparser.generator.java.JavaMethodBuilder
import com._7aske.strapparser.generator.spring.SpringJavaPackages.OPENAPI_PACKAGE
import com._7aske.strapparser.generator.spring.SpringJavaPackages.SPRINGDOC_PACKAGE
import com._7aske.strapparser.generator.spring.SpringJavaPackages.SPRING_BIND_PACKAGE
import com._7aske.strapparser.generator.spring.SpringJavaPackages.SPRING_DOMAIN_PACKAGE
import com._7aske.strapparser.generator.spring.SpringJavaPackages.SPRING_HTTP_PACKAGE
import com.google.googlejavaformat.java.Formatter
import java.nio.file.Path
import java.nio.file.Paths

private const val SECURITY_REQ = "@SecurityRequirement(name = \"bearerAuth\")"

class SpringJavaApiDocGeneratorImpl(
    private val controllerGenerator: ControllerGenerator,
    ctx: GeneratorContext,
    dataTypeResolver: DataTypeResolver
) : JavaClassGenerator(ctx, dataTypeResolver) {
    private val entityClassName =
        controllerGenerator.getEntityGenerator().getClassName()
    private val entityVariableName =
        controllerGenerator.getEntityGenerator().getVariableName()

    private val formatter = Formatter()

    init {
        import("$OPENAPI_PACKAGE.Operation")
        import("$OPENAPI_PACKAGE.Parameter")
        import("$OPENAPI_PACKAGE.tags.Tag")
        import("$SPRING_BIND_PACKAGE.*")
        import("$SPRING_DOMAIN_PACKAGE.Page")
        import("$SPRING_DOMAIN_PACKAGE.Pageable")
        import("$SPRING_HTTP_PACKAGE.ResponseEntity")
        import("$SPRINGDOC_PACKAGE.ParameterObject")
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
        "java",
        getPackage().replace(".", separator),
        this.getClassName() + ".java"
    )

    override fun generate(): String = formatter.formatSource(
        buildString {
            append("package ${getPackage()};")
            append(getImports())
            append("@Tag(name = \"${entityVariableName}\", description = \"$entityClassName API\")")
            append("public interface ").append(getClassName()).append(" {")
            append(generateGetEndpoints())
            append(generatePostEndpoints())
            append(generatePutEndpoints())
            append(generateDeleteEndpoints())
            append("}")
        }
    )

    private fun generateGetEndpoints(): String = buildString {
        append(
            JavaMethodBuilder.abstract("getAll${entityClassName.plural()}")
                .apply {
                    annotations.add(
                        "@Operation(tags = \"${entityVariableName}\"," +
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

        append(
            JavaMethodBuilder.abstract("get${entityClassName}ById").apply {
                annotations.add(
                    "@Operation(tags = \"${entityVariableName}\"," +
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
        append(
            JavaMethodBuilder.abstract("save$entityClassName").apply {
                annotations.add(
                    "@Operation(tags = \"${entityVariableName}\"," +
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
        append(
            JavaMethodBuilder.abstract("update$entityClassName").apply {
                annotations.add(
                    "@Operation(tags = \"${entityVariableName}\"," +
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

        append(
            JavaMethodBuilder.abstract("delete${entityClassName}ById").apply {
                annotations.add(
                    "@Operation(tags = \"${entityVariableName}\"," +
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

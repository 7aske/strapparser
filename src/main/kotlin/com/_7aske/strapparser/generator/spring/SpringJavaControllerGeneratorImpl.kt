package com._7aske.strapparser.generator.spring

import com._7aske.strapparser.extensions.capitalize
import com._7aske.strapparser.extensions.plural
import com._7aske.strapparser.extensions.toKebabCase
import com._7aske.strapparser.extensions.uncapitalize
import com._7aske.strapparser.generator.ControllerGenerator
import com._7aske.strapparser.generator.DataTypeResolver
import com._7aske.strapparser.generator.EntityGenerator
import com._7aske.strapparser.generator.GeneratorContext
import com.google.googlejavaformat.java.Formatter
import java.nio.file.Path
import java.nio.file.Paths

const val RESPONSE_ENTITY_FQCN = "org.springframework.http.ResponseEntity"
const val SPRING_BIND_PACKAGE = "org.springframework.web.bind.annotation"
const val SPRING_DOMAIN_PACKAGE = "org.springframework.data.domain"

class SpringJavaControllerGeneratorImpl(
    entity: EntityGenerator,
    ctx: GeneratorContext,
    dataTypeResolver: DataTypeResolver
) : ControllerGenerator(entity, ctx, dataTypeResolver) {
    private val formatter = Formatter()
    private val serviceVarName: String
    private val serviceClassName: String

    init {
        serviceVarName = entity.resolveClassName().uncapitalize() + "Service"
        serviceClassName =
            "${ctx.getPackageName("service")}.${entity.resolveClassName()}Service"
    }

    override fun getOutputFilePath(): Path = Paths.get(
        ctx.getOutputLocation(),
        "src",
        "main",
        "java",
        resolvePackage().replace(".", separator),
        this.resolveClassName() + ".java"
    )

    override fun generate(): String {
        return formatter.formatSource(
            buildString {
                append("package ${resolvePackage()};")
                append("@$SPRING_BIND_PACKAGE.RestController\n")
                append("@$SPRING_BIND_PACKAGE.RequestMapping(\"/api/v1/${resolveEndpoint()}\")\n")
                append("public class ").append(resolveClassName())
                append("{")
                append("private final ").append(serviceClassName).append(" ")
                append(serviceVarName).append(";")
                append("public ").append(resolveClassName()).append("(")
                append(serviceClassName).append(" ")
                append(serviceVarName)
                append(") {")
                append("this.")
                append(serviceVarName)
                append("=")
                append(serviceVarName).append(";")
                append("}")
                append(generateEndpoints())
                append("}")
            }
        )
    }

    override fun resolveVariableName(): String =
        resolveClassName().uncapitalize()

    override fun resolveEndpoint(): String =
        this.entity.resolveClassName().toKebabCase().uncapitalize().plural()

    override fun resolveClassName(): String =
        this.entity.resolveClassName() + "Controller"

    override fun resolvePackage(): String = ctx.getPackageName("controller")

    override fun resolveFQCN(): String =
        resolvePackage() + "." + resolveClassName()

    private fun generateGetEndpoints(): String = buildString {
        append("@$SPRING_BIND_PACKAGE.GetMapping\n")
        append("public ").append("$RESPONSE_ENTITY_FQCN<$SPRING_DOMAIN_PACKAGE.Page<${entity.resolveFQCN()}>>")
        append(
            "getAll${
            entity.resolveVariableName().plural().capitalize()
            }("
        )
        append(SPRING_DOMAIN_PACKAGE).append(".Pageable page")
        append(") {")
        append("return $RESPONSE_ENTITY_FQCN.ok($serviceVarName.findAll(page));")
        append("}")

        append(
            "@$SPRING_BIND_PACKAGE.GetMapping(\"/${
            entity.resolveIdFieldPathVariables()
            }\")\n"
        )
        append("public ").append("$RESPONSE_ENTITY_FQCN<${entity.resolveFQCN()}>")
        append(
            "get${
            entity.resolveVariableName().capitalize()
            }ById("
        )
        append(resolveIdFieldsParameters())
        append(") {")
        append(
            "return $RESPONSE_ENTITY_FQCN.ok($serviceVarName.findById(${
            entity.resolveIdFieldVariables()
            }));"
        )
        append("}")
    }

    private fun generatePostEndpoints(): String = buildString {
        append(
            "@$SPRING_BIND_PACKAGE.PostMapping\n"
        )
        append("public ").append("$RESPONSE_ENTITY_FQCN<${entity.resolveFQCN()}>")
        append(
            "save${
            entity.resolveVariableName().capitalize()
            }("
        )
        append("@$SPRING_BIND_PACKAGE.RequestBody ").append(entity.resolveFQCN())
            .append(" ").append(entity.resolveVariableName())
        append(") {")
        append("return ")
        append(RESPONSE_ENTITY_FQCN)
        append(
            ".status(org.springframework.http.HttpStatus.CREATED).body("
        )
        append(serviceVarName)
        append(".save(")
        append(entity.resolveVariableName())
        append("));")
        append("}")
    }

    private fun generatePutEndpoints(): String = buildString {
        append(
            "@$SPRING_BIND_PACKAGE.PutMapping\n"
        )
        append("public ").append("$RESPONSE_ENTITY_FQCN<${entity.resolveFQCN()}>")
        append(
            "update${
            entity.resolveVariableName().capitalize()
            }("
        )
        append("@$SPRING_BIND_PACKAGE.RequestBody ").append(entity.resolveFQCN())
            .append(" ").append(entity.resolveVariableName())
        append(") {")
        append(
            "return $RESPONSE_ENTITY_FQCN.ok($serviceVarName.update(${
            entity.resolveVariableName()
            }));"
        )
        append("}")
    }

    private fun generateDeleteEndpoints(): String = buildString {
        append(
            "@$SPRING_BIND_PACKAGE.DeleteMapping(\"/${
            entity.resolveIdFieldPathVariables()
            }\")\n"
        )
        append("public ").append("$RESPONSE_ENTITY_FQCN<Void>")
        append(
            "delete${
            entity.resolveVariableName().capitalize()
            }ById("
        )
        append(resolveIdFieldsParameters())
        append(") {")
        append(
            "$serviceVarName.deleteById(${
            entity.resolveIdFieldVariables()
            });"
        )
        append("return $RESPONSE_ENTITY_FQCN.noContent().build();")
        append("}")
    }

    override fun generateEndpoints(): String = buildString {
        append(generateGetEndpoints())
        append(generatePostEndpoints())
        append(generatePutEndpoints())
        append(generateDeleteEndpoints())
    }

    private fun resolveIdFieldsParameters(): String =
        entity.getIdFields().joinToString(", ") {
            buildString {
                append("@$SPRING_BIND_PACKAGE.PathVariable")
                append(" ")
                append(
                    dataTypeResolver.resolveDataType(it)
                )
                append(" ")
                append(it.name)
            }
        }
}

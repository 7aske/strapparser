package com._7aske.strapparser.generator.spring

import com._7aske.strapparser.extensions.capitalize
import com._7aske.strapparser.extensions.plural
import com._7aske.strapparser.extensions.toKebabCase
import com._7aske.strapparser.extensions.uncapitalize
import com._7aske.strapparser.generator.*
import com._7aske.strapparser.generator.java.Lombok
import com.google.googlejavaformat.java.Formatter
import java.nio.file.Path
import java.nio.file.Paths

const val RESPONSE_ENTITY_FQCN = "org.springframework.http.ResponseEntity"
const val SPRING_BIND_PACKAGE = "org.springframework.web.bind.annotation"
const val SPRING_DOMAIN_PACKAGE = "org.springframework.data.domain"
const val REST_CONTROLLER_ANNOTATION = "@$SPRING_BIND_PACKAGE.RestController\n"

class SpringJavaControllerGeneratorImpl(
    service: ServiceGenerator,
    ctx: GeneratorContext,
    dataTypeResolver: DataTypeResolver
) : ControllerGenerator(service, ctx, dataTypeResolver) {
    private val formatter = Formatter()

    private val entity: EntityGenerator
    private val serviceVarName: String
    private val serviceFQCN: String

    init {
        entity = service.entity
        serviceVarName = service.resolveVariableName()
        serviceFQCN = service.resolveFQCN()
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
                append(REST_CONTROLLER_ANNOTATION)
                append(Mapping.request(resolveEndpoint()))

                if (ctx.args.lombok) {
                    append(Lombok.RequiredArgsConstructor)
                }

                append("public class ").append(resolveClassName())
                append("{")
                append("private final ").append(serviceFQCN).append(" ")
                append(serviceVarName).append(";")
                if (!ctx.args.lombok) {
                    append("public ").append(resolveClassName()).append("(")
                    append(serviceFQCN).append(" ")
                    append(serviceVarName)
                    append(") {")
                    append("this.")
                    append(serviceVarName)
                    append("=")
                    append(serviceVarName).append(";")
                    append("}")
                }
                append(generateEndpoints())
                append("}")
            }
        )
    }

    override fun resolveVariableName(): String =
        resolveClassName().uncapitalize()

    override fun resolveEndpoint(): String =
        "/api/v1/" + entity.resolveClassName()
            .toKebabCase()
            .uncapitalize()
            .plural()

    override fun resolveClassName(): String =
        entity.resolveClassName() + "Controller"

    override fun resolvePackage(): String = ctx.getPackageName("controller")

    override fun resolveFQCN(): String =
        resolvePackage() + "." + resolveClassName()

    private fun generateGetEndpoints(): String = buildString {
        append(Mapping.get())
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

        append(Mapping.get(entity.resolveIdFieldPathVariables()))
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
        append(Mapping.post())
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
        append(Mapping.put())
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
        append(Mapping.delete(entity.resolveIdFieldPathVariables()))
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

class Mapping private constructor(
    private val path: String,
    private val type: String,
) {
    companion object {
        fun request(path: String) = Mapping(path, "RequestMapping")
        fun get(path: String = "") = Mapping(path, "GetMapping")
        fun post(path: String = "") = Mapping(path, "PostMapping")
        fun put(path: String = "") = Mapping(path, "PutMapping")
        fun delete(path: String = "") = Mapping(path, "DeleteMapping")
    }

    override fun toString(): String {
        val prefix = "@$SPRING_BIND_PACKAGE.$type"

        if (path.isEmpty())
            return prefix + "\n"

        return "$prefix(\"$path\")\n"
    }
}

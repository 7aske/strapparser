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
        serviceVarName = service.getVariableName()
        serviceFQCN = service.getFQCN()
    }

    override fun getOutputFilePath(): Path = Paths.get(
        ctx.getOutputLocation(),
        "src",
        "main",
        "java",
        getPackage().replace(".", separator),
        this.getClassName() + ".java"
    )

    override fun generate(): String {
        return formatter.formatSource(
            buildString {
                append("package ${getPackage()};")
                append(REST_CONTROLLER_ANNOTATION)
                append(Mapping.request(resolveEndpoint()))

                if (ctx.args.lombok) {
                    append(Lombok.RequiredArgsConstructor)
                }

                append("public class ").append(getClassName())
                append("{")
                append("private final ").append(serviceFQCN).append(" ")
                append(serviceVarName).append(";")
                if (!ctx.args.lombok) {
                    append("public ").append(getClassName()).append("(")
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

    override fun getVariableName(): String =
        getClassName().uncapitalize()

    override fun resolveEndpoint(): String =
        "/api/v1/" + entity.getClassName()
            .toKebabCase()
            .uncapitalize()
            .plural()

    override fun getClassName(): String =
        entity.getClassName() + "Controller"

    override fun getPackage(): String = ctx.getPackageName("controller")

    override fun getFQCN(): String =
        getPackage() + "." + getClassName()

    private fun generateGetEndpoints(): String = buildString {
        append(Mapping.get())
        append("public ").append("$RESPONSE_ENTITY_FQCN<$SPRING_DOMAIN_PACKAGE.Page<${entity.getFQCN()}>>")
        append(
            "getAll${
            entity.getVariableName().plural().capitalize()
            }("
        )
        append(SPRING_DOMAIN_PACKAGE).append(".Pageable page")
        append(") {")
        append("return $RESPONSE_ENTITY_FQCN.ok($serviceVarName.findAll(page));")
        append("}")

        append(Mapping.get(entity.getIdFieldPathVariables()))
        append("public ").append("$RESPONSE_ENTITY_FQCN<${entity.getFQCN()}>")
        append(
            "get${
            entity.getVariableName().capitalize()
            }ById("
        )
        append(resolveIdFieldsParameters())
        append(") {")
        append(
            "return $RESPONSE_ENTITY_FQCN.ok($serviceVarName.findById(${
            entity.getIdFieldVariables()
            }));"
        )
        append("}")
    }

    private fun generatePostEndpoints(): String = buildString {
        append(Mapping.post())
        append("public ").append("$RESPONSE_ENTITY_FQCN<${entity.getFQCN()}>")
        append(
            "save${
            entity.getVariableName().capitalize()
            }("
        )
        append("@$SPRING_BIND_PACKAGE.RequestBody ").append(entity.getFQCN())
            .append(" ").append(entity.getVariableName())
        append(") {")
        append("return ")
        append(RESPONSE_ENTITY_FQCN)
        append(
            ".status(org.springframework.http.HttpStatus.CREATED).body("
        )
        append(serviceVarName)
        append(".save(")
        append(entity.getVariableName())
        append("));")
        append("}")
    }

    private fun generatePutEndpoints(): String = buildString {
        append(Mapping.put())
        append("public ").append("$RESPONSE_ENTITY_FQCN<${entity.getFQCN()}>")
        append(
            "update${
            entity.getVariableName().capitalize()
            }("
        )
        append("@$SPRING_BIND_PACKAGE.RequestBody ").append(entity.getFQCN())
            .append(" ").append(entity.getVariableName())
        append(") {")
        append(
            "return $RESPONSE_ENTITY_FQCN.ok($serviceVarName.update(${
            entity.getVariableName()
            }));"
        )
        append("}")
    }

    private fun generateDeleteEndpoints(): String = buildString {
        append(Mapping.delete(entity.getIdFieldPathVariables()))
        append("public ").append("$RESPONSE_ENTITY_FQCN<Void>")
        append(
            "delete${
            entity.getVariableName().capitalize()
            }ById("
        )
        append(resolveIdFieldsParameters())
        append(") {")
        append(
            "$serviceVarName.deleteById(${
            entity.getIdFieldVariables()
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

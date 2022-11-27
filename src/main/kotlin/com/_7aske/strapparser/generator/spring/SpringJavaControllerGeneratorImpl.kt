package com._7aske.strapparser.generator.spring

import com._7aske.strapparser.extensions.capitalize
import com._7aske.strapparser.extensions.plural
import com._7aske.strapparser.extensions.toKebabCase
import com._7aske.strapparser.extensions.uncapitalize
import com._7aske.strapparser.generator.*
import com._7aske.strapparser.generator.java.JavaMethodBuilder
import com._7aske.strapparser.generator.java.Lombok
import com.google.googlejavaformat.java.Formatter
import java.nio.file.Path
import java.nio.file.Paths

const val RESPONSE_ENTITY = "org.springframework.http.ResponseEntity"
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
        "api/v1/" + entity.getClassName()
            .toKebabCase()
            .uncapitalize()
            .plural()

    override fun getClassName(): String =
        entity.getClassName() + "Controller"

    override fun getPackage(): String = ctx.getPackageName("controller")

    override fun getFQCN(): String =
        getPackage() + "." + getClassName()

    private fun generateGetEndpoints(): String = buildString {
        val entitySingular = entity.getVariableName().capitalize()
        val entityPlural = entitySingular.plural()

        append(
            JavaMethodBuilder.of("getAll$entityPlural").apply {
                annotations.add(Mapping.get().toString())
                returnType =
                    "$RESPONSE_ENTITY<$SPRING_DOMAIN_PACKAGE.Page<${entity.getFQCN()}>>"
                parameters.add(listOf("$SPRING_DOMAIN_PACKAGE.Pageable", "page"))
                implementation =
                    "return $RESPONSE_ENTITY.ok($serviceVarName.findAll(page));"
            }
        )

        append(
            JavaMethodBuilder.of("get${entitySingular}ById").apply {
                annotations.add(
                    Mapping.get(entity.getIdFieldPathVariables()).toString()
                )
                returnType = "$RESPONSE_ENTITY<${entity.getFQCN()}>"
                parameters.addAll(resolveIdFieldsParameters())
                implementation =
                    "return $RESPONSE_ENTITY.ok($serviceVarName.findById(${
                    entity.getIdFieldVariables()
                    }));"
            }
        )
    }

    private fun generatePostEndpoints(): String = buildString {
        val entitySingular = entity.getVariableName().capitalize()

        append(
            JavaMethodBuilder.of("save$entitySingular").apply {
                annotations.add(Mapping.post().toString())
                returnType = "$RESPONSE_ENTITY<${entity.getFQCN()}>"
                parameters.add(
                    listOf(
                        "@$SPRING_BIND_PACKAGE.RequestBody",
                        entity.getFQCN(),
                        entity.getVariableName()
                    )
                )
                implementation =
                    "return $RESPONSE_ENTITY.status(org.springframework.http.HttpStatus.CREATED).body($serviceVarName.save(${entity.getVariableName()}));"
            }
        )
    }

    private fun generatePutEndpoints(): String = buildString {
        val entitySingular = entity.getVariableName().capitalize()

        append(
            JavaMethodBuilder.of("update$entitySingular").apply {
                annotations.add(Mapping.put().toString())
                returnType = "$RESPONSE_ENTITY<${entity.getFQCN()}>"
                parameters.add(
                    listOf(
                        "@$SPRING_BIND_PACKAGE.RequestBody",
                        entity.getFQCN(),
                        entity.getVariableName()
                    )
                )
                implementation =
                    "return $RESPONSE_ENTITY.ok($serviceVarName.update(${entity.getVariableName()}));"
            }
        )
    }

    private fun generateDeleteEndpoints(): String = buildString {
        val entitySingular = entity.getVariableName().capitalize()

        append(
            JavaMethodBuilder.of("delete${entitySingular}ById").apply {
                annotations.add(
                    Mapping.delete(entity.getIdFieldPathVariables()).toString()
                )
                returnType = "$RESPONSE_ENTITY<Void>"
                parameters.addAll(resolveIdFieldsParameters())
                implementation =
                    "$serviceVarName.deleteById(${entity.getIdFieldVariables()});" +
                    "return $RESPONSE_ENTITY.noContent().build();"
            }
        )
    }

    override fun generateEndpoints(): String = buildString {
        append(generateGetEndpoints())
        append(generatePostEndpoints())
        append(generatePutEndpoints())
        append(generateDeleteEndpoints())
    }

    private fun resolveIdFieldsParameters(): List<List<String>> =
        entity.getIdFields().map {
            listOf(
                "@$SPRING_BIND_PACKAGE.PathVariable",
                dataTypeResolver.resolveDataType(it), it.name
            )
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

        return "$prefix(\"/$path\")\n"
    }
}

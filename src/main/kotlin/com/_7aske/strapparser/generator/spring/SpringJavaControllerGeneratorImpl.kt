package com._7aske.strapparser.generator.spring

import com._7aske.strapparser.extensions.capitalize
import com._7aske.strapparser.extensions.plural
import com._7aske.strapparser.extensions.toKebabCase
import com._7aske.strapparser.extensions.uncapitalize
import com._7aske.strapparser.generator.*
import com._7aske.strapparser.generator.java.JavaMethodBuilder
import com._7aske.strapparser.generator.java.Lombok
import com._7aske.strapparser.generator.spring.SpringJavaPackages.SPRING_BIND_PACKAGE
import com._7aske.strapparser.generator.spring.SpringJavaPackages.SPRING_DOMAIN_PACKAGE
import com._7aske.strapparser.generator.spring.SpringJavaPackages.SPRING_HTTP_PACKAGE
import com.google.googlejavaformat.java.Formatter
import java.nio.file.Path
import java.nio.file.Paths


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
        import("$SPRING_BIND_PACKAGE.*")
        import("$SPRING_DOMAIN_PACKAGE.*")
        import("$SPRING_HTTP_PACKAGE.ResponseEntity")
        import("$SPRING_HTTP_PACKAGE.HttpStatus")
        import("$SPRING_HTTP_PACKAGE.HttpHeaders")
        import(serviceFQCN)
        import(entity.getFQCN())

        if (ctx.args.lombok) {
            import(Lombok.PACKAGE + ".*")
        }
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
                append(getImports())
                append("@RestController")
                append(Mapping.request(resolveEndpoint()))

                if (ctx.args.lombok) {
                    append(Lombok.RequiredArgsConstructor)
                }

                append("public class ").append(getClassName())
                append("{")
                append("private final ").append(service.getClassName()).append(" ")
                append(serviceVarName).append(";")
                if (!ctx.args.lombok) {
                    append("public ").append(getClassName()).append("(")
                    append(service.getClassName()).append(" ")
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
                    "ResponseEntity<Page<${entity.getClassName()}>>"
                parameters.add(listOf("Pageable", "page"))
                implementation =
                    "return ResponseEntity.ok($serviceVarName.findAll(page));"
            }
        )

        append(
            JavaMethodBuilder.of("get${entitySingular}ById").apply {
                annotations.add(
                    Mapping.get(entity.getIdFieldPathVariables()).toString()
                )
                returnType = "ResponseEntity<${entity.getClassName()}>"
                parameters.addAll(resolveIdFieldsParameters())
                implementation =
                    "return ResponseEntity.ok($serviceVarName.findById(${
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
                returnType = "ResponseEntity<${entity.getClassName()}>"
                parameters.add(
                    listOf(
                        "@RequestBody",
                        entity.getClassName(),
                        entity.getVariableName()
                    )
                )
                implementation =
                    "return ResponseEntity.status(HttpStatus.CREATED)" +
                    ".header(HttpHeaders.LOCATION, \"${resolveEndpoint()}/\" + ${entity.getVariableName()}.getId())" +
                    ".body($serviceVarName.save(${entity.getVariableName()}));"
            }
        )
    }

    private fun generatePutEndpoints(): String = buildString {
        val entitySingular = entity.getVariableName().capitalize()

        append(
            JavaMethodBuilder.of("update$entitySingular").apply {
                annotations.add(Mapping.put().toString())
                returnType = "ResponseEntity<${entity.getClassName()}>"
                parameters.add(
                    listOf(
                        "@RequestBody",
                        entity.getClassName(),
                        entity.getVariableName()
                    )
                )
                implementation =
                    "return ResponseEntity.ok($serviceVarName.update(${entity.getVariableName()}));"
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
                returnType = "ResponseEntity<Void>"
                parameters.addAll(resolveIdFieldsParameters())
                implementation =
                    "$serviceVarName.deleteById(${entity.getIdFieldVariables()});" +
                    "return ResponseEntity.noContent().build();"
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
                "@PathVariable",
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
        val prefix = "@$type"

        if (path.isEmpty())
            return prefix + "\n"

        return "$prefix(\"/$path\")\n"
    }
}

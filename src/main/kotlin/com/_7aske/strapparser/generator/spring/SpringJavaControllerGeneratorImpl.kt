package com._7aske.strapparser.generator.spring

import com._7aske.strapparser.extensions.capitalize
import com._7aske.strapparser.extensions.plural
import com._7aske.strapparser.extensions.toKebabCase
import com._7aske.strapparser.extensions.uncapitalize
import com._7aske.strapparser.generator.DataTypeResolver
import com._7aske.strapparser.generator.EntityGenerator
import com._7aske.strapparser.generator.GeneratorContext
import com._7aske.strapparser.generator.ServiceGenerator
import com._7aske.strapparser.generator.java.Formatter
import com._7aske.strapparser.generator.java.JavaClassGenerator
import com._7aske.strapparser.generator.java.JavaMethodBuilder
import com._7aske.strapparser.generator.java.Lombok
import com._7aske.strapparser.generator.spring.SpringPackages.SPRING_BIND_PACKAGE
import com._7aske.strapparser.generator.spring.SpringPackages.SPRING_DOMAIN_PACKAGE
import com._7aske.strapparser.generator.spring.SpringPackages.SPRING_HTTP_PACKAGE
import java.nio.file.Path
import java.nio.file.Paths

class SpringJavaControllerGeneratorImpl(
    internal val service: ServiceGenerator,
    ctx: GeneratorContext,
    dataTypeResolver: DataTypeResolver
) : JavaClassGenerator(ctx, dataTypeResolver), SpringControllerGenerator {

    private val formatter = Formatter()
    private val entity: EntityGenerator = service.entity
    private val serviceVarName: String = service.getVariableName()
    private val serviceFQCN: String = service.getFQCN()

    init {
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
        if (ctx.args.doc) {
            import("${ctx.getPackageName("api")}.${entity.getClassName()}Api")
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
                if (ctx.args.doc) {
                    append(" implements ")
                    append(entity.getClassName())
                    append("Api")
                }
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

    override fun resolveEndpoint(): String =
        "api/v1/" + entity.getClassName()
            .toKebabCase()
            .uncapitalize()
            .plural()

    override fun getEntityGenerator(): EntityGenerator =
        entity

    override fun getClassName(): String =
        entity.getClassName() + "Controller"

    override fun getPackage(): String = ctx.getPackageName("controller")

    private fun generateGetEndpoints(): String = buildString {
        val entitySingular = entity.getVariableName().capitalize()
        val entityPlural = entitySingular.plural()

        append(
            JavaMethodBuilder.of("getAll$entityPlural").apply {
                if (ctx.args.doc) {
                    annotations.add("@Override")
                }
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
                if (ctx.args.doc) {
                    annotations.add("@Override")
                }
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
                if (ctx.args.doc) {
                    annotations.add("@Override")
                }
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
                if (ctx.args.doc) {
                    annotations.add("@Override")
                }
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
                if (ctx.args.doc) {
                    annotations.add("@Override")
                }
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

    override fun resolveIdFieldsParameters(): List<List<String>> =
        entity.getIdFields().map {
            listOf(
                "@PathVariable",
                dataTypeResolver.resolveDataType(it), it.name
            )
        }
}

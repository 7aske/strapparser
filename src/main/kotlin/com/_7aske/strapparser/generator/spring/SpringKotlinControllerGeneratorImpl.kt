package com._7aske.strapparser.generator.spring

import com._7aske.strapparser.extensions.capitalize
import com._7aske.strapparser.extensions.plural
import com._7aske.strapparser.extensions.toKebabCase
import com._7aske.strapparser.extensions.uncapitalize
import com._7aske.strapparser.generator.*
import com._7aske.strapparser.generator.kotlin.Formatter
import com._7aske.strapparser.generator.kotlin.KotlinClassGenerator
import com._7aske.strapparser.generator.kotlin.KotlinMethodBuilder
import com._7aske.strapparser.generator.spring.SpringPackages.SPRING_BIND_PACKAGE
import com._7aske.strapparser.generator.spring.SpringPackages.SPRING_DOMAIN_PACKAGE
import com._7aske.strapparser.generator.spring.SpringPackages.SPRING_HTTP_PACKAGE
import java.nio.file.Path
import java.nio.file.Paths

class SpringKotlinControllerGeneratorImpl(
    internal val service: SpringKotlinServiceGeneratorImpl,
    ctx: GeneratorContext,
    dataTypeResolver: DataTypeResolver
) : KotlinClassGenerator(ctx, dataTypeResolver), SpringControllerGenerator {

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

        if (ctx.args.doc) {
            import("${ctx.getPackageName("api")}.${entity.getClassName()}Api")
        }
    }

    override fun getOutputFilePath(): Path = Paths.get(
        ctx.getOutputLocation(),
        "src",
        "main",
        "kotlin",
        getPackage().replace(".", separator),
        this.getClassName() + ".kt"
    )

    override fun generate(): String {
        return formatter.formatSource(
            buildString {
                appendLine("package ${getPackage()}")
                append(getImports())
                appendLine("@RestController")
                appendLine(Mapping.request(resolveEndpoint()))

                append("class ").append(getClassName())
                append("( private val ")
                append(serviceVarName)
                append(": ")
                append(service.getClassName())
                append(")")

                if (ctx.args.doc) {
                    append(" : ")
                    append(entity.getClassName())
                    append("Api")
                }

                append("{")
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
            KotlinMethodBuilder.of("getAll$entityPlural").apply {
                if (ctx.args.doc) {
                    override = true
                }
                annotations.add(Mapping.get().toString())
                returnType =
                    "ResponseEntity<Page<${entity.getClassName()}>>"
                parameters.add(listOf("Pageable", "page"))
                implementation =
                    "return ResponseEntity.ok($serviceVarName.findAll(page))"
            }
        )

        append(
            KotlinMethodBuilder.of("get${entitySingular}ById").apply {
                if (ctx.args.doc) {
                    override = true
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
            KotlinMethodBuilder.of("save$entitySingular").apply {
                if (ctx.args.doc) {
                    override = true
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
                    ".header(HttpHeaders.LOCATION, \"${resolveEndpoint()}/\" + ${entity.getVariableName()}.id)" +
                    ".body($serviceVarName.save(${entity.getVariableName()}))"
            }
        )
    }

    private fun generatePutEndpoints(): String = buildString {
        val entitySingular = entity.getVariableName().capitalize()

        append(
            KotlinMethodBuilder.of("update$entitySingular").apply {
                if (ctx.args.doc) {
                    override = true
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
                    "return ResponseEntity.ok($serviceVarName.update(${entity.getVariableName()}))"
            }
        )
    }

    private fun generateDeleteEndpoints(): String = buildString {
        val entitySingular = entity.getVariableName().capitalize()

        append(
            KotlinMethodBuilder.of("delete${entitySingular}ById").apply {
                if (ctx.args.doc) {
                    override = true
                }
                annotations.add(
                    Mapping.delete(entity.getIdFieldPathVariables()).toString()
                )
                returnType = "ResponseEntity<Void>"
                parameters.addAll(resolveIdFieldsParameters())
                implementation =
                    "$serviceVarName.deleteById(${entity.getIdFieldVariables()})\n" +
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

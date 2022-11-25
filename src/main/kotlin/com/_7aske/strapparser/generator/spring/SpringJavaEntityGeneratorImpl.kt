package com._7aske.strapparser.generator.spring

import com._7aske.strapparser.extensions.capitalize
import com._7aske.strapparser.extensions.plural
import com._7aske.strapparser.extensions.uncapitalize
import com._7aske.strapparser.generator.DataTypeResolver
import com._7aske.strapparser.generator.EntityGenerator
import com._7aske.strapparser.generator.GeneratorContext
import com._7aske.strapparser.parser.TokenType
import com._7aske.strapparser.parser.definitions.Entity
import com._7aske.strapparser.parser.definitions.Field
import com.google.googlejavaformat.java.Formatter
import java.nio.file.Path
import java.nio.file.Paths

class SpringJavaEntityGeneratorImpl(
    entity: Entity,
    ctx: GeneratorContext,
    dataTypeResolver: DataTypeResolver
) : EntityGenerator(entity, ctx, dataTypeResolver) {
    private val formatter = Formatter()

    override fun getOutputFilePath(): Path =
        Paths.get(
            ctx.getOutputLocation(),
            "src",
            "main",
            "java",
            ctx.getPackageName().replace(".", separator),
            "entity",
            this.resolveClassName() + ".java"
        )

    override fun generate(): String {
        return formatter.formatSourceAndFixImports(
            buildString {
                append("package ${resolvePackage()};")
                append("@jakarta.persistence.Table @jakarta.persistence.Entity\n")
                append("public class ")
                    .append(resolveClassName())
                append("{")

                append(
                    entity.fields.joinToString("\n") {
                        generateField(it)
                    }
                )

                val referenced = ctx.getEntityFieldsThatReference(entity.name)
                append(
                    referenced.joinToString("\n") {
                        generateField(it)
                    }
                )

                append(
                    entity.fields.joinToString("\n") {
                        generateGetter(it) +
                            generateSetter(it)
                    }
                )

                append(
                    referenced.joinToString("\n") {
                        generateGetter(it) +
                            generateSetter(it)
                    }
                )

                append("}")
            }
        )
    }

    override fun getIdFields(): List<Field> =
        entity.fields
            .filter { it.attributes.any { attr -> attr.token.type == TokenType.ID } }
            .toList()

    override fun generateField(field: Field): String {
        return buildString {
            // @Todo handle many to many
            var type = field.type.value

            if (field.attributes.any { it.token.type == TokenType.ID }) {
                append("@jakarta.persistence.Id\n")
            }

            if (field.attributes.any { it.token.type == TokenType.SERIAL }) {
                append("@jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)\n")
            }

            if (field.isRef()) {
                append("@jakarta.persistence.ManyToOne\n")
                append("@jakarta.persistence.JoinColumn\n")
                append("private $type ${resolveVariableName(field)};\n")
            } else if (field.isList()) {
                append("@jakarta.persistence.OneToMany\n")
                append("private java.util.List<$type> ${resolveVariableName(field)};\n")
            } else {
                append("@jakarta.persistence.Column\n")
                type = dataTypeResolver.resolveDataType(type)
                append("private $type ${resolveVariableName(field)};\n")
            }
        }
    }

    override fun generateSetter(field: Field): String {
        val varName = resolveVariableName(field)
        return buildString {
            append("public void set${resolveVariableName(field).capitalize()}")
            append("(${dataTypeResolver.resolveDataType(field)} $varName){")
            append("this.$varName = $varName;")
            append("}")
        }
    }

    override fun generateGetter(field: Field): String {
        return buildString {
            append(
                "public ${dataTypeResolver.resolveDataType(field)} get${
                resolveVariableName(field).capitalize()
                }"
            )
            append("(){")
            append("return this.${resolveVariableName(field)};")
            append("}")
        }
    }

    override fun resolveClassName(): String =
        entity.name.capitalize()

    override fun resolvePackage(): String =
        ctx.getPackageName("entity")

    override fun resolveFQCN(): String =
        resolvePackage() + "." + resolveClassName()

    override fun resolveVariableName(): String =
        entity.name.uncapitalize()

    override fun resolveVariableName(field: Field): String {
        return if (field.isList()) {
            field.name.plural()
        } else {
            field.name
        }
    }

    override fun resolveFieldSetter(field: Field): String {
        TODO("Not yet implemented")
    }

    override fun resolveFieldGetter(field: Field): String {
        TODO("Not yet implemented")
    }

    override fun resolveIdFieldPathVariables() =
        getIdFields().joinToString("/") {
            "{${it.name}}"
        }

    override fun resolveIdFieldVariables() =
        getIdFields().joinToString(", ") {
            it.name
        }

    override fun resolveIdFieldsParameters() =
        getIdFields().joinToString(", ") {
            buildString {
                append(
                    dataTypeResolver.resolveDataType(it)
                )
                append(" ")
                append(it.name)
            }
        }
}

package com._7aske.strapparser.generator.spring

import com._7aske.strapparser.extensions.capitalize
import com._7aske.strapparser.extensions.plural
import com._7aske.strapparser.extensions.uncapitalize
import com._7aske.strapparser.generator.EntityGenerator
import com._7aske.strapparser.generator.GeneratorContext
import com._7aske.strapparser.parser.TokenType
import com._7aske.strapparser.parser.definitions.Entity
import com._7aske.strapparser.parser.definitions.Field
import com.google.googlejavaformat.java.Formatter
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.Paths

class SpringJavaEntityGeneratorImpl(
    private val entity: Entity,
    private val ctx: GeneratorContext
) : EntityGenerator {
    private val formatter = Formatter()
    private val dataTypeResolver = SpringJavaDataTypeResolverImpl()
    private val separator = FileSystems.getDefault().separator

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

    override fun generateEntity(): String {
        return formatter.formatSourceAndFixImports(
            buildString {
                append("package ${ctx.getPackageName("entity")};")
                append("@javax.persistence.Table @javax.persistence.Entity\n")
                append("public class ")
                    .append(resolveClassName())
                append("{")

                append(
                    entity.fields.joinToString("\n") {
                        generateField(it, ctx)
                    }
                )

                val referenced = ctx.getEntityFieldsThatReference(entity.name)
                append(
                    referenced.joinToString("\n") {
                        generateField(it, ctx)
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

    override fun generateField(field: Field, ctx: GeneratorContext): String {
        return buildString {
            // @Todo handle many to many
            var type = field.type.value

            if (field.attributes.any { it.token.type == TokenType.ID }) {
                append("@javax.persistence.Id\n")
            }

            if (field.attributes.any { it.token.type == TokenType.SERIAL }) {
                append("@javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)\n")
            }

            if (field.isRef()) {
                append("@javax.persistence.ManyToOne\n")
                append("@javax.persistence.JoinColumn\n")
            } else if (field.isList()) {
                append("@javax.persistence.OneToMany\n")
            } else {
                append("@javax.persistence.Column\n")
                type = dataTypeResolver.resolveDataType(type)
            }
            append("private $type ${resolveVariableName(field)};\n")
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
}

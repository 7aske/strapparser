package com._7aske.strapparser.generator.spring

import com._7aske.strapparser.extensions.capitalize
import com._7aske.strapparser.extensions.plural
import com._7aske.strapparser.extensions.uncapitalize
import com._7aske.strapparser.generator.Constants
import com._7aske.strapparser.generator.DataTypeResolver
import com._7aske.strapparser.generator.EntityGenerator
import com._7aske.strapparser.generator.GeneratorContext
import com._7aske.strapparser.generator.kotlin.Formatter
import com._7aske.strapparser.generator.kotlin.KotlinMethodBuilder
import com._7aske.strapparser.parser.TokenType
import com._7aske.strapparser.parser.definitions.Entity
import com._7aske.strapparser.parser.definitions.Field
import java.nio.file.Path
import java.nio.file.Paths

class SpringKotlinEntityGeneratorImpl(
    entity: Entity,
    ctx: GeneratorContext,
    dataTypeResolver: DataTypeResolver
) : EntityGenerator(entity, ctx, dataTypeResolver) {
    private val formatter = Formatter()

    init {
        import("java.io.Serializable")
        import("jakarta.persistence.*")
        import("com.fasterxml.jackson.annotation.JsonIgnoreProperties")

        if (entity.isUserDetails() && ctx.args.security) {
            import("org.springframework.security.core.userdetails.UserDetails")
            import("org.springframework.security.core.GrantedAuthority")
            import("com.fasterxml.jackson.annotation.JsonIgnore")
        }
    }

    override fun getOutputFilePath(): Path =
        Paths.get(
            ctx.getOutputLocation(),
            "src",
            "main",
            "kotlin",
            ctx.getPackageName().replace(".", separator),
            "entity",
            this.getClassName() + ".kt"
        )

    override fun generate(): String {
        val implements = mutableListOf<String>()
        val extends = mutableListOf<String>()
        if (ctx.args.auditable) {
            // It will be in the same package
            extends.add("Auditable()")
        } else {
            implements.add("Serializable")
        }

        if (entity.isUserDetails() && ctx.args.security) {
            implements.add("UserDetails")
        }

        return formatter.formatSource(
            buildString {
                appendLine("package ${getPackage()}")
                appendLine(getImports())
                append("\n\n")
                appendLine("@Table(name = \"${entity.getTableName()}\")")
                appendLine("@Entity")
                append("open class ")
                    .append(getClassName())
                appendLine("(")

                // This can be refactored to not check composite id twice
                if (hasCompositeId()) {
                    appendLine("@EmbeddedId")
                    append("var")
                    append(" id: ")
                    append(getIdClassName())
                    appendLine(",\n")
                }

                val toGenerate: List<Field> = getFieldsToGenerate()

                appendLine(
                    toGenerate.joinToString("\n") {
                        generateField(it)
                    }
                )

                val referenced = ctx.getEntityFieldsThatReference(entity.name)
                appendLine(
                    referenced.joinToString("\n") {
                        generateField(it)
                    }
                )

                append(")")

                if (extends.isNotEmpty() || implements.isNotEmpty()) {
                    appendLine(" : ")
                    appendLine((extends + implements).joinToString(","))
                }
                append("{")

                if (entity.isUserDetails() && ctx.args.security) {
                    appendLine(generateUserDetailsGettersAndSetters())
                }

                appendLine("}")

                if (hasCompositeId()) {
                    appendLine(generateCompositeId())
                }
            }
        )
    }

    private fun getFieldsToGenerate() = if (hasCompositeId()) {
        entity.fields
            .filter { !it.isId() }
            .filter {
                if (entity.isUserDetails() && ctx.args.security) {
                    !it.isOfType(TokenType.USERNAME) && !it.isOfType(
                        TokenType.PASSWORD
                    )
                }
                true
            }
    } else {
        entity.fields
    }

    private fun generateUserDetailsGettersAndSetters(): String {
        return buildString {
            append(
                KotlinMethodBuilder.getter(
                    "authorities",
                    "Collection<GrantedAuthority>"
                ).apply {
                    annotations.add("@JsonIgnore")
                    override = true
                    implementation = "return Collections.emptyList()"
                }
            )

            append(
                KotlinMethodBuilder.of("getUsername").apply {
                    override = true
                    returnType = "String"
                    implementation =
                        "return ${entity.getUsernameField()?.name ?: "null"}"
                }
            )

            append(
                KotlinMethodBuilder.of("getPassword").apply {
                    annotations.add("@JsonIgnore")
                    override = true
                    returnType = "String"
                    implementation = "return ${getPasswordField()}"
                }
            )

            append(
                KotlinMethodBuilder.of("isAccountNonExpired").apply {
                    annotations.add("@JsonIgnore")
                    override = true
                    returnType = "Boolean"
                    implementation = "return isEnabled"
                }
            )
            append(
                KotlinMethodBuilder.of("isAccountNonLocked").apply {
                    annotations.add("@JsonIgnore")
                    override = true
                    returnType = "Boolean"
                    implementation = "return isEnabled"
                }
            )
            append(
                KotlinMethodBuilder.of("isCredentialsNonExpired").apply {
                    annotations.add("@JsonIgnore")
                    override = true
                    returnType = "Boolean"
                    implementation = "return isEnabled"
                }
            )
            append(
                KotlinMethodBuilder.of("isEnabled").apply {
                    annotations.add("@JsonIgnore")
                    override = true
                    returnType = "Boolean"
                    implementation = "return true"
                }
            )
        }
    }

    private fun generateCompositeId(): String {
        return buildString {
            appendLine("@Embeddable\n")
            appendLine(
                "open class ${getIdClassName()}("
            )

            val idFields = getIdFields()
            idFields.forEach {
                appendLine(generateField(it).replace("@Id", ""))
            }
            append(") : Serializable")
        }
    }

    override fun getIdFields(): List<Field> =
        entity.fields
            .filter { it.isId() }
            .toList()

    override fun generateField(field: Field): String {
        return buildString {
            var type = field.type.value

            if (field.isId()) {
                appendLine("@Id")
            }

            if (field.attributes.any { it.token.type == TokenType.SERIAL }) {
                appendLine("@GeneratedValue(strategy = GenerationType.IDENTITY)")
            }

            if (field.isRef()) {
                appendLine("@JsonIgnoreProperties(\"${getVariableName().plural()}\")")
                appendLine("@ManyToOne")
                appendLine("@JoinColumn(name=\"${field.getColumnName()}\")")
                appendLine("var ${getVariableName(field)}: $type,")
            } else if (field.isList()) {
                val referenced = ctx.getReferencedEntity(type)
                if (referenced != null && referenced.fields.any {
                    it.isList() && it.type.value == entity.name
                }
                ) {
                    // many to many
                    appendLine("@JsonIgnoreProperties(\"${getVariableName().plural()}\")")
                    appendLine("@ManyToMany")
                    appendLine(
                        "@JoinTable(name=\"${
                        getJoinTable(
                            referenced.getTableName(),
                            entity.getTableName()
                        )
                        }\", " +
                            "joinColumns=[JoinColumn(name=\"${referenced.getTableName()}" +
                            "${Constants.MANY_TO_MANY_COLUMN_SUFFIX}\")], " +
                            "inverseJoinColumns=[JoinColumn(name=\"${entity.getTableName()}" +
                            "${Constants.MANY_TO_MANY_COLUMN_SUFFIX}\")]" +
                            ")"
                    )
                    appendLine(
                        "var ${
                        getVariableName(
                            field
                        )
                        }: MutableList<$type>,"
                    )
                } else {
                    appendLine("@JsonIgnoreProperties(\"${entity.name.uncapitalize()}\")\n")
                    appendLine("@OneToMany(mappedBy = \"${entity.name.uncapitalize()}\")\n")
                    appendLine(
                        "var ${
                        getVariableName(
                            field
                        )
                        }: MutableList<$type>,"
                    )
                }
            } else {
                appendLine("@Column(name = \"${field.getColumnName()}\")")
                type = dataTypeResolver.resolveDataType(type)
                if (field.isOfType(TokenType.USERNAME) || field.isOfType(
                        TokenType.PASSWORD
                    )
                ) {
                    appendLine("private ")
                }
                appendLine("var ${getVariableName(field)}: $type,")
            }
        }
    }

    private fun getJoinTable(first: String, second: String): String {
        return listOf(first, second)
            .sorted()
            .joinToString("_")
            .lowercase()
    }

    override fun generateSetter(field: Field): String {
        val varName = getVariableName(field)
        return buildString {
            append(
                "public void set${
                getVariableName(field).capitalize()
                }"
            )
            append("(${dataTypeResolver.resolveDataType(field)} $varName){")
            append("this.$varName = $varName;")
            append("}")
        }
    }

    override fun generateGetter(field: Field): String {
        return buildString {
            append(
                "public ${dataTypeResolver.resolveDataType(field)} get${
                getVariableName(field).capitalize()
                }"
            )
            append("(){")
            append("return this.${getVariableName(field)};")
            append("}")
        }
    }

    override fun getClassName(): String =
        entity.name.capitalize()

    override fun getPackage(): String =
        ctx.getPackageName("entity")

    override fun getVariableName(field: Field): String {
        return if (field.isList()) {
            field.name.plural()
        } else {
            field.name
        }
    }

    override fun getIdFQCN(): String {
        return if (hasCompositeId()) {
            getPackage() + "." + getIdClassName()
        } else {
            val field = getIdFields()[0]
            dataTypeResolver.resolveDataType(field)
        }
    }

    override fun getIdClassName(): String {
        return if (hasCompositeId()) {
            getClassName() + "Id"
        } else {
            val field = getIdFields()[0]
            dataTypeResolver.resolveDataType(field)
        }
    }

    override fun getIdFieldPathVariables() =
        getIdFields().joinToString("/") {
            "{${it.name}}"
        }

    override fun getCompositeIdFieldVariables() = if (hasCompositeId()) {
        "${getIdFQCN()}(${getIdFieldVariables()})"
    } else {
        getIdFieldVariables()
    }

    override fun getIdFieldVariables() =
        getIdFields().joinToString(", ") {
            it.name
        }

    override fun getIdFieldsAsArguments() =
        getIdFields().joinToString(", ") {
            buildString {
                append(it.name)
                append(": ")
                append(
                    dataTypeResolver.resolveDataType(it)
                )
            }
        }

    override fun hasCompositeId() =
        getIdFields().size > 1

    private fun getPasswordField(): String {
        return entity.fields.filter { it.isOfType(TokenType.PASSWORD) }
            .map { it.name }
            .lastOrNull() ?: "null"
    }
}

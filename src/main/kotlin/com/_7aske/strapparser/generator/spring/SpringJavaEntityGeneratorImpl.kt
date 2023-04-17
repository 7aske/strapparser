package com._7aske.strapparser.generator.spring

import com._7aske.strapparser.extensions.capitalize
import com._7aske.strapparser.extensions.plural
import com._7aske.strapparser.extensions.uncapitalize
import com._7aske.strapparser.generator.Constants
import com._7aske.strapparser.generator.DataTypeResolver
import com._7aske.strapparser.generator.EntityGenerator
import com._7aske.strapparser.generator.GeneratorContext
import com._7aske.strapparser.generator.java.Formatter
import com._7aske.strapparser.generator.java.JavaMethodBuilder
import com._7aske.strapparser.generator.java.Lombok
import com._7aske.strapparser.parser.TokenType
import com._7aske.strapparser.parser.definitions.Entity
import com._7aske.strapparser.parser.definitions.Field
import java.nio.file.Path
import java.nio.file.Paths

class SpringJavaEntityGeneratorImpl(
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
        if (ctx.args.lombok) {
            import(Lombok.PACKAGE + ".*")
        }
    }

    override fun getOutputFilePath(): Path =
        Paths.get(
            ctx.getOutputLocation(),
            "src",
            "main",
            "java",
            ctx.getPackageName().replace(".", separator),
            "entity",
            this.getClassName() + ".java"
        )

    override fun generate(): String {
        val implements = mutableListOf<String>()
        val extends = mutableListOf<String>()
        if (ctx.args.auditable) {
            // It will be in the same package
            extends.add("Auditable")
        } else {
            implements.add("Serializable")
        }

        if (entity.isUserDetails() && ctx.args.security) {
            implements.add("UserDetails")
        }

        return formatter.formatSource(
            buildString {
                append("package ${getPackage()};")
                append(getImports())
                append("@Table(name = \"${entity.getTableName()}\")\n")
                append("@Entity\n")
                if (ctx.args.lombok) {
                    append(Lombok.Data)
                    append(Lombok.RequiredArgsConstructor)
                    append(Lombok.EqualsAndHashCode)
                }
                append("public class ")
                    .append(getClassName())

                check(extends.size <= 1) {
                    "Java classes can only extend one base class"
                }
                if (extends.isNotEmpty()) {
                    append(" extends ${extends[0]}")
                }
                if (implements.isNotEmpty()) {
                    append(" implements ")
                    append(implements.joinToString(","))
                }
                append("{")

                // This can be refactored to not check composite id twice
                if (hasCompositeId()) {
                    append("@EmbeddedId\n")
                    append("private ")
                    append(getIdClassName())
                    append(" id;")
                }

                val toGenerate: List<Field> = getFieldsToGenerate()

                append(
                    toGenerate.joinToString("\n") {
                        generateField(it)
                    }
                )

                val referenced = ctx.getEntityFieldsThatReference(entity.name)
                append(
                    referenced.joinToString("\n") {
                        generateField(it)
                    }
                )

                if (!ctx.args.lombok) {
                    (toGenerate + referenced).forEach {
                        if (it.attributes.any { attr ->
                            attr.value == "username" || attr.value == "password"
                        }
                        ) {
                            return@forEach
                        }

                        val name =
                            if (it.isList()) it.name.plural() else it.name
                        append(
                            JavaMethodBuilder.getter(
                                name,
                                dataTypeResolver.resolveDataType(it)
                            )
                        )
                        append(
                            JavaMethodBuilder.setter(
                                name,
                                dataTypeResolver.resolveDataType(it)
                            )
                        )
                    }
                }

                if (entity.isUserDetails() && ctx.args.security) {
                    append(generateUserDetailsGettersAndSetters())
                }

                if (hasCompositeId()) {
                    append(generateCompositeId())
                }

                append("}")
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
                JavaMethodBuilder.getter(
                    "authorities",
                    "Collection<? extends GrantedAuthority>"
                ).apply {
                    annotations.add("@JsonIgnore")
                    annotations.add("@Override")
                    implementation = "return Collections.emptyList();"
                }
            )

            append(
                JavaMethodBuilder.of("getUsername").apply {
                    annotations.add("@Override")
                    returnType = "String"
                    implementation = "return ${entity.getUsernameField()?.name ?: "null"};"
                }
            )

            append(
                JavaMethodBuilder.of("getPassword").apply {
                    annotations.add("@JsonIgnore")
                    annotations.add("@Override")
                    returnType = "String"
                    implementation = "return ${getPasswordField()};"
                }
            )

            append(
                JavaMethodBuilder.of("isAccountNonExpired").apply {
                    annotations.add("@JsonIgnore")
                    annotations.add("@Override")
                    returnType = "boolean"
                    implementation = "return isEnabled();"
                }
            )
            append(
                JavaMethodBuilder.of("isAccountNonLocked").apply {
                    annotations.add("@JsonIgnore")
                    annotations.add("@Override")
                    returnType = "boolean"
                    implementation = "return isEnabled();"
                }
            )
            append(
                JavaMethodBuilder.of("isCredentialsNonExpired").apply {
                    annotations.add("@JsonIgnore")
                    annotations.add("@Override")
                    returnType = "boolean"
                    implementation = "return isEnabled();"
                }
            )
            append(
                JavaMethodBuilder.of("isEnabled").apply {
                    annotations.add("@JsonIgnore")
                    annotations.add("@Override")
                    returnType = "boolean"
                    implementation = "return true;"
                }
            )
        }
    }

    private fun generateCompositeId(): String {
        return buildString {
            if (ctx.args.lombok) {
                append(Lombok.Data)
                append(Lombok.AllArgsConstructor)
                append(Lombok.NoArgsConstructor)
                append(Lombok.EqualsAndHashCode)
            }
            append("@Embeddable\n")
            append(
                "public static final class ${getIdClassName()} implements Serializable {"
            )

            val idFields = getIdFields()
            idFields.forEach {
                append(generateField(it))
            }

            if (!ctx.args.lombok) {
                append("public ").append(getIdClassName())
                    .append("(){}")
                append("public ").append(getIdClassName()).append("(")
                append(
                    idFields.joinToString(", ") {
                        "${dataTypeResolver.resolveDataType(it)} ${it.name}"
                    }
                )
                append(") {")

                append(
                    idFields.joinToString("\n") {
                        "this.${it.name} = ${it.name};"
                    }
                )

                append("}")

                append(
                    idFields.joinToString("\n") {
                        generateGetter(it) +
                            generateSetter(it)
                    }
                )
            }

            append("}")
        }
    }

    override fun getIdFields(): List<Field> =
        entity.fields
            .filter { it.isId() }
            .toList()

    override fun generateField(field: Field): String {
        return buildString {
            // @Todo handle many to many
            var type = field.type.value

            if (field.isId()) {
                if (ctx.args.lombok) {
                    append(Lombok.EqualsAndHashCodeInclude)
                }
                append("@Id\n")
            }

            if (field.attributes.any { it.token.type == TokenType.SERIAL }) {
                append("@GeneratedValue(strategy = GenerationType.IDENTITY)\n")
            }

            if (field.isRef()) {
                if (ctx.args.lombok) {
                    append(Lombok.ToStringExclude)
                }
                append("@JsonIgnoreProperties(\"${getVariableName().plural()}\")\n")
                append("@ManyToOne\n")
                append("@JoinColumn\n")
                append("private $type ${getVariableName(field)};\n")
            } else if (field.isList()) {
                if (ctx.args.lombok) {
                    append(Lombok.ToStringExclude)
                }

                val referenced = ctx.getReferencedEntity(type)
                if (referenced != null && referenced.fields.any {
                    it.isList() && it.type.value == entity.name
                }
                ) {
                    // many to many
                    append("@JsonIgnoreProperties(\"${getVariableName().plural()}\")\n")
                    append("@ManyToMany\n")
                    append(
                        "@JoinTable(name=\"${getJoinTable(referenced.getTableName(), entity.getTableName())}\", " +
                            "joinColumns=@JoinColumn(name=\"${referenced.getTableName()}" +
                            "${Constants.MANY_TO_MANY_COLUMN_SUFFIX}\"), " +
                            "inverseJoinColumns=@JoinColumn(name=\"${entity.getTableName()}" +
                            "${Constants.MANY_TO_MANY_COLUMN_SUFFIX}\")" +
                            ")\n"
                    )
                    append(
                        "private List<$type> ${
                        getVariableName(
                            field
                        )
                        };\n"
                    )
                } else {
                    append("@JsonIgnoreProperties(\"${entity.name.uncapitalize()}\")\n")
                    append("@OneToMany(mappedBy = \"${entity.name.uncapitalize()}\")\n")
                    append(
                        "private List<$type> ${
                        getVariableName(
                            field
                        )
                        };\n"
                    )
                }
            } else {
                append("@Column(name = \"${field.getColumnName()}\")\n")
                type = dataTypeResolver.resolveDataType(type)
                append("private $type ${getVariableName(field)};\n")
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
            getPackage() + "." + getClassName() + "." + getIdClassName()
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
        "new ${getIdFQCN()}(${getIdFieldVariables()})"
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
                append(
                    dataTypeResolver.resolveDataType(it)
                )
                append(" ")
                append(it.name)
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

package com._7aske.strapparser.generator.spring

import com._7aske.strapparser.extensions.capitalize
import com._7aske.strapparser.extensions.plural
import com._7aske.strapparser.extensions.uncapitalize
import com._7aske.strapparser.generator.DataTypeResolver
import com._7aske.strapparser.generator.EntityGenerator
import com._7aske.strapparser.generator.GeneratorContext
import com._7aske.strapparser.generator.java.JavaMethodBuilder
import com._7aske.strapparser.generator.java.Lombok
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
            this.getClassName() + ".java"
        )

    override fun generate(): String {
        val implements = mutableListOf<String>()
        val extends = mutableListOf<String>()
        if (ctx.args.auditable) {
            extends.add("${getPackage()}.Auditable")
        } else {
            implements.add("java.io.Serializable")
        }

        if (entity.isUserDetails() && ctx.args.security) {
            implements.add("org.springframework.security.core.userdetails.UserDetails")
        }

        return formatter.formatSourceAndFixImports(
            buildString {
                append("package ${getPackage()};")
                append("@jakarta.persistence.Table\n")
                append("@jakarta.persistence.Entity\n")
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
                    append("@jakarta.persistence.EmbeddedId\n")
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
                        append(
                            JavaMethodBuilder.getter(
                                it.name,
                                dataTypeResolver.resolveDataType(it)
                            )
                        )
                        append(
                            JavaMethodBuilder.setter(
                                it.name,
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
                    "java.util.Collection<? extends org.springframework.security.core.GrantedAuthority>"
                ).apply {
                    annotations.add("@Override")
                    implementation = "return java.util.Collections.emptyList();"
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
                    annotations.add("@Override")
                    returnType = "String"
                    implementation = "return ${getPasswordField()};"
                }
            )

            append(
                JavaMethodBuilder.of("isAccountNonExpired").apply {
                    returnType = "boolean"
                    implementation = "return isEnabled();"
                }
            )
            append(
                JavaMethodBuilder.of("isAccountNonLocked").apply {
                    returnType = "boolean"
                    implementation = "return isEnabled();"
                }
            )
            append(
                JavaMethodBuilder.of("isCredentialsNonExpired").apply {
                    returnType = "boolean"
                    implementation = "return isEnabled();"
                }
            )
            append(
                JavaMethodBuilder.of("isEnabled").apply {
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
            append("@jakarta.persistence.Embeddable\n")
            append(
                "public static final class ${getIdClassName()} implements java.io.Serializable {"
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
                append("@jakarta.persistence.Id\n")
            }

            if (field.attributes.any { it.token.type == TokenType.SERIAL }) {
                append("@jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)\n")
            }

            if (field.isRef()) {
                if (ctx.args.lombok) {
                    append(Lombok.ToStringExclude)
                }
                append("@jakarta.persistence.ManyToOne\n")
                append("@jakarta.persistence.JoinColumn\n")
                append("private $type ${getVariableName(field)};\n")
            } else if (field.isList()) {
                if (ctx.args.lombok) {
                    append(Lombok.ToStringExclude)
                }
                append("@jakarta.persistence.OneToMany(mappedBy = \"${entity.name.uncapitalize()}\")\n")
                append(
                    "private java.util.List<$type> ${
                    getVariableName(
                        field
                    )
                    };\n"
                )
            } else {
                append("@jakarta.persistence.Column\n")
                type = dataTypeResolver.resolveDataType(type)
                append("private $type ${getVariableName(field)};\n")
            }
        }
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

    override fun getFQCN(): String =
        getPackage() + "." + getClassName()

    override fun getVariableName(): String =
        entity.name.uncapitalize()

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

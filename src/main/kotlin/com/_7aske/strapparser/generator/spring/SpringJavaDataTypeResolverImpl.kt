package com._7aske.strapparser.generator.spring

import com._7aske.strapparser.generator.DataTypeResolver
import com._7aske.strapparser.parser.definitions.Field

class SpringJavaDataTypeResolverImpl : DataTypeResolver {
    private val types = mapOf(
        "string" to "String",
        "varchar" to "String",
        "text" to "String",
        "char" to "Character",
        "int" to "Integer",
        "integer" to "Integer",
        "long" to "Long",
        "float" to "Float",
        "double" to "Double",
        "decimal" to "Double",
        "real" to "Double",
        "bool" to "Boolean",
        "boolean" to "Boolean",
        "timestamp" to "java.time.Instant",
        "date" to "java.time.LocalDate",
        "datetime" to "java.time.LocalDateTime",
        "time" to "java.time.LocalTime",
    )

    override fun resolveDataType(type: String): String =
        types[type] ?: type

    override fun resolveDataType(field: Field): String {
        return if (field.isList()) {
            return resolveDataTypeAsList(field.type.value)
        } else {
            resolveDataType(field.type.value)
        }
    }

    override fun resolveDataTypeAsList(type: String): String =
        "java.util.List<${resolveDataType(type)}>"
}

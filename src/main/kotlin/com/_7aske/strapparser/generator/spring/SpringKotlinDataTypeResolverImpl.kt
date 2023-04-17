package com._7aske.strapparser.generator.spring

import com._7aske.strapparser.generator.DataTypeResolver
import com._7aske.strapparser.parser.definitions.Field

class SpringKotlinDataTypeResolverImpl : DataTypeResolver {
    private val types = mapOf(
        "string" to "String",
        "varchar" to "String",
        "text" to "String",
        "char" to "Char",
        "int" to "Int",
        "integer" to "Int",
        "long" to "Long",
        "float" to "Float",
        "double" to "Double",
        "decimal" to "Double",
        "real" to "Double",
        "bool" to "Boolean",
        "boolean" to "Boolean",
        "timestamp" to "Instant",
        "date" to "LocalDate",
        "datetime" to "LocalDateTime",
        "time" to "LocalTime",
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
        "MutableList<${resolveDataType(type)}>"
}

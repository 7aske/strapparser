package com._7aske.strapparser.generator

import com._7aske.strapparser.parser.definitions.Field

interface DataTypeResolver {
    fun resolveDataType(type: String): String

    fun resolveDataType(field: Field): String

    fun resolveDataTypeAsList(type: String): String
}

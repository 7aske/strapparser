package com._7aske.strapparser.parser.definitions

import com._7aske.strapparser.parser.Token

class Field(
    token: Token,
    val name: String,
    var type: FieldType,
    val attributes: List<Attribute>
) : Definition(token) {
    fun isRef() = type is RefFieldType
    fun isList() = type is RefFieldType
    fun isRegular() = !isRef() && !isList()
}
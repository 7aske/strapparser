package com._7aske.strapparser.parser.definitions

import com._7aske.strapparser.parser.Token
import com._7aske.strapparser.parser.TokenType

class Field(
    token: Token,
    val name: String,
    var type: FieldType,
    val attributes: List<Attribute>
) : Definition(token) {

    fun isOfType(type: TokenType): Boolean =
        attributes.any { it.token.type == type }

    fun isId() = attributes.any { it.token.type == TokenType.ID }

    fun isRef() = type is RefFieldType

    fun isList() = type is ListFieldType

    fun isRegular() = !isRef() && !isList()

    fun getReferencedEntityName(): String {
        check(isList() || isRef())
        return type.value
    }
}

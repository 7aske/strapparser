package com._7aske.strapparser.parser.definitions

import com._7aske.strapparser.parser.Token
import com._7aske.strapparser.parser.TokenType

class Entity(
    token: Token,
    val name: String,
    var fields: List<Field>,
    private val attributes: List<Attribute>
) : Definition(token) {

    fun getTableName(): String {
        val tableAttribute = attributes.firstOrNull { it.token.type == TokenType.TABLE }
        return tableAttribute?.value ?: name
    }

    fun isUserDetails(): Boolean =
        attributes.any { it.token.type == TokenType.USER_DETAILS }

    fun getUsernameField(): Field? {
        return fields.lastOrNull { it.isOfType(TokenType.USERNAME) }
    }
}

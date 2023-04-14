package com._7aske.strapparser.parser.ast

import com._7aske.strapparser.parser.Token

class AstAttributeNode(override val token: Token) : AstNode(token) {
    var tokenValue: Token? = null

    constructor(token: Token, value: Token) : this(token) {
        this.tokenValue = value
    }

    fun getValue(): String = tokenValue?.value ?: token.value

    override fun toString(): String {
        return "AstAttributeNode($token, $tokenValue)"
    }
}

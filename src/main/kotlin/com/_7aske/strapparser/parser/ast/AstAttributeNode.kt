package com._7aske.strapparser.parser.ast

import com._7aske.strapparser.parser.Token

class AstAttributeNode(override val token: Token) : AstNode(token) {
    override fun toString(): String {
        return "AstAttributeNode($token)"
    }
}

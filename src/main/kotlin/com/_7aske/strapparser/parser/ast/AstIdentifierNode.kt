package com._7aske.strapparser.parser.ast

import com._7aske.strapparser.parser.Token

class AstIdentifierNode(override val token: Token) : AstNode(token) {
    override fun toString(): String {
        return "AstIdentifierNode($token)"
    }
}

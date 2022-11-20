package com._7aske.strapparser.parser.ast

import com._7aske.strapparser.parser.Token

class AstListNode(override val token: Token, val typeNode: AstIdentifierNode) : AstNode(token) {
    override fun toString(): String {
        return "AstListNode($token, $typeNode)"
    }
}

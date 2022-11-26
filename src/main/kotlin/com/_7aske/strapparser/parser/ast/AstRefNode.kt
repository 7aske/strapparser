package com._7aske.strapparser.parser.ast

import com._7aske.strapparser.parser.Token

class AstRefNode(override val token: Token, val typeNode: AstIdentifierNode) :
    AstNode(token) {
    override fun toString(): String {
        return "AstRefNode($token, $typeNode)"
    }
}

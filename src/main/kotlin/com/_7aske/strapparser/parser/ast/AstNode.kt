package com._7aske.strapparser.parser.ast

import com._7aske.strapparser.parser.Token

open class AstNode(open val token: Token) {
    override fun toString(): String {
        return "AstNode($token)"
    }
}
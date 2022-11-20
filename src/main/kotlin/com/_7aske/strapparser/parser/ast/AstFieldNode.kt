package com._7aske.strapparser.parser.ast

import com._7aske.strapparser.parser.Token

class AstFieldNode(
    override val token: Token,
    val name: AstNode,
    val type: AstNode,
    val attributes: List<AstNode>,
) : AstNode(token) {
    override fun toString(): String {
        return "AstFieldNode($token, $name, $type, $attributes)"
    }
}

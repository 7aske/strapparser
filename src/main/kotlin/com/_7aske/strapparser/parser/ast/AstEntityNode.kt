package com._7aske.strapparser.parser.ast

import com._7aske.strapparser.parser.Token

open class AstEntityNode(
    override val token: Token,
    val name: AstNode,
    val fields: MutableList<AstFieldNode>,
    val attributes: List<AstNode>,
) : AstNode(token) {
    override fun toString(): String {
        return "AstEntityNode($token, $name, $fields, $attributes)"
    }
}

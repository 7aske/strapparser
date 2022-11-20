package com._7aske.strapparser.parser

class Token(
    val type: TokenType,
    val value: String,
    val startChar: Int,
    val endChar: Int,
    val startRow: Int
) {
    override fun toString(): String {
        val sanitizedValue = value
            .replace("\n", "\\n")
            .replace("\t", "\\t")

        return "${type.name}('$sanitizedValue')@$startChar-$endChar:$startRow"
    }
}
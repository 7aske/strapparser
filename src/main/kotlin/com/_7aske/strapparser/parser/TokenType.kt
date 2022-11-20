package com._7aske.strapparser.parser

enum class TokenType(val keyword: String) {
    ENTITY("entity"),
    FIELD("field"),
    LIST("list"),
    SERIAL("serial"),
    REFERENCES("references"),
    OWNER("owner"),
    UNIQUE("unique"),
    SPACE(" "),
    TAB("\t"),
    NEWLINE("\n"),
    IDENTIFIER("<IDEN>");

    companion object {
        private val valuesMap: Map<String, TokenType> =
            values().associateBy { it.keyword }

        fun tryParse(value: String): TokenType? =
            valuesMap[value]

        fun tryParse(value: Char): TokenType? =
            tryParse(value.toString())
    }

    override fun toString(): String =
        when (keyword) {
            "\n" -> "\\n"
            "\t" -> "\\t"
            else -> "'$keyword'"
        }


}
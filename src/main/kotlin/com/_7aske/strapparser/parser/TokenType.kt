package com._7aske.strapparser.parser

enum class TokenType(val keyword: String) {
    EQUALS("="),
    ENTITY("entity"),
    FIELD("field"),
    LIST("list"),
    SERIAL("serial"),
    USER_DETAILS("userDetails"),
    ID("id"),
    REFERENCES("references"),
    OWNER("owner"),
    UNIQUE("unique"),
    SPACE(" "),
    TAB("\t"),
    NEWLINE("\n"),
    IDENTIFIER("<IDEN>"),
    TYPE("<TYPE>"),
    USERNAME("username"),
    PASSWORD("password"),
    TABLE("table"),
    COLUMN("column");

    companion object {
        private val valuesMap: Map<String, TokenType> =
            values().associateBy { it.keyword }

        fun tryParse(value: String): TokenType? =
            valuesMap[value]

        fun tryParse(value: Char): TokenType? =
            tryParse(value.toString())

        val entityAttributeTypes = arrayOf(
            USER_DETAILS,
            TABLE
        )

        val fieldAttributeTypes = arrayOf(
            USERNAME,
            PASSWORD,
            UNIQUE,
            SERIAL,
            OWNER,
            ID,
            COLUMN
        )
    }

    override fun toString(): String =
        when (keyword) {
            "\n" -> "\\n"
            "\t" -> "\\t"
            else -> "'$keyword'"
        }
}

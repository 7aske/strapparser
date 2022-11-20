package com._7aske.strapparser.parser

import com._7aske.strapparser.parser.iter.IndexedStringIterator

class Lexer(input: String) : IndexedStringIterator(input) {
    private val tokens = mutableListOf<Token>()
    val onEmit: (token: Token) -> Unit = {}

    private fun emit(type: TokenType) = emit(type, type.keyword)
    private fun emit(type: TokenType, value: String) {
        val token = Token(type, value, char - value.length, char, row)
        println(token)
        tokens.add(token)
        onEmit(token)
    }

    fun lex() : MutableList<Token> {
        while (hasNext()) {
            eatSpace()

            if (isPeek('\t')) {
                TokenType.tryParse(next())
                    ?.also { emit(it) }
                continue
            }

            if (isPeek('\n')) {
                TokenType.tryParse(next())
                    ?.also { emit(it) }
                continue
            }

            val word = eatWord()
            TokenType.tryParse(word)?.also {
                emit(it)
            } ?: run {
                emit(TokenType.IDENTIFIER, word)
            }
        }

        return tokens
    }
}
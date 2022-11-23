package com._7aske.strapparser.parser

import com._7aske.strapparser.parser.iter.IndexedStringIterator
import com._7aske.strapparser.util.ParserUtil.Companion.printLocation

class Lexer(input: String) : IndexedStringIterator(input) {
    private val tokens = mutableListOf<Token>()

    private fun emit(type: TokenType) = emit(type, type.keyword)
    private fun emit(type: TokenType, value: String) {
        val token = Token(type, value, char - value.length, char, row)
        tokens.add(token)
    }

    fun lex(): MutableList<Token> {
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

            if (isPeek('`')) {
                val word = eatIdentifier()
                emit(TokenType.IDENTIFIER, word)
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

    private fun eatIdentifier(): String {
        check(isPeek('`')) {
            "Identifier must start with '`'"
        }

        next() // skip `

        val word = eatWord()

        check(isPeek('`')) {
            printLocation(content, row, char - word.length - 1, char)
            "Unterminated identifier"
        }
        next() // skip `

        check(word.isNotEmpty()) {
            printLocation(content, row, char - 2, char - 1)
            "Zero length identifier"
        }
        return word
    }
}

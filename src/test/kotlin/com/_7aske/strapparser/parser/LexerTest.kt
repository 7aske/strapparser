package com._7aske.strapparser.parser

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class LexerTest {


    @Test
    fun `test lexing`() {
        val text = "entity User\n\tfield id integer serial"

        val lexer = Lexer(text)

        val tokens = lexer.lex()

        assertEquals(TokenType.ENTITY, tokens[0].type)
        assertEquals(TokenType.IDENTIFIER, tokens[1].type)
        assertEquals(TokenType.NEWLINE, tokens[2].type)
        assertEquals(TokenType.TAB, tokens[3].type)
        assertEquals(TokenType.FIELD, tokens[4].type)
        assertEquals(TokenType.IDENTIFIER, tokens[5].type)
        assertEquals(TokenType.IDENTIFIER, tokens[6].type)
        assertEquals(TokenType.SERIAL, tokens[7].type)
    }
}
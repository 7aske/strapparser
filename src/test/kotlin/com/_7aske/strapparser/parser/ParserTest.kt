package com._7aske.strapparser.parser

import com._7aske.strapparser.parser.ast.AstEntityNode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ParserTest {

    @Test
    fun `test parse`() {
        val text = """
            entity User
                field `id` int serial
                field roles list Role
                
            entity Post
                field `id` int serial
                field content string
                field user references User
                
            entity Role
                field `id` int serial
                field name string unique
        """.trimIndent()

        val lexer = Lexer(text)
        val tokens = lexer.lex()

        val parser = Parser(text, tokens)
        val ast = parser.parse()

        assertEquals(TokenType.ENTITY, ast[0].token.type)
        assertEquals(
            TokenType.FIELD,
            (ast[0] as AstEntityNode).fields[0].token.type
        )
        assertEquals(TokenType.ENTITY, ast[1].token.type)
        assertEquals(
            TokenType.FIELD,
            (ast[1] as AstEntityNode).fields[0].token.type
        )
        assertEquals(
            TokenType.FIELD,
            (ast[1] as AstEntityNode).fields[1].token.type
        )
    }

    @Test
    fun `parser throw on missing field keyword`() {
        val text = """
            entity Role
                id int serial
                field name string unique
        """.trimIndent()

        val lexer = Lexer(text)
        val tokens = lexer.lex()

        val parser = Parser(text, tokens)
        assertThrows<IllegalStateException> {
            parser.parse()
        }
    }

    @Test
    fun `parser throw on missing entity keyword`() {
        val text = """
            Role
                field name string unique
        """.trimIndent()

        val lexer = Lexer(text)
        val tokens = lexer.lex()

        val parser = Parser(text, tokens)
        assertThrows<IllegalStateException> {
            parser.parse()
        }
    }
}

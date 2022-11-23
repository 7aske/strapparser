package com._7aske.strapparser.parser

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class InterpreterTest {

    @Test
    fun interpret() {
        val text = """
            entity Post
                field `id` int serial
                field content string
                field user references User
                
            entity User
                field `id` int serial
                field roles list Role
                
            entity Role
                field `id` int serial
                field name string unique
        """.trimIndent()

        val lexer = Lexer(text)
        val tokens = lexer.lex()

        val parser = Parser(text, tokens)
        val ast = parser.parse()

        val interpreter = Interpreter(text, ast)
        val entities = interpreter.interpret()

        assertEquals(3, entities.size)
        assertEquals("Post", entities[0].name)
        assertEquals("User", entities[1].name)
        assertEquals("Role", entities[2].name)
    }
}

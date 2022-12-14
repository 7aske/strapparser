package com._7aske.strapparser.parser

import java.nio.file.Files
import java.nio.file.Path

class StrapFileResolver {

    fun resolve(path: Path): List<com._7aske.strapparser.parser.definitions.Entity> {
        val text = Files.readString(path)

        val lexer = Lexer(text)
        val tokens = lexer.lex()

        val parser = Parser(text, tokens)
        val ast = parser.parse()

        val interpreter = Interpreter(text, ast)
        return interpreter.interpret()
    }
}

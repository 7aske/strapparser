package com._7aske.strapparser.parser

import com._7aske.strapparser.generator.translation.TranslationStrategy
import com._7aske.strapparser.parser.definitions.Entity
import java.nio.file.Files
import java.nio.file.Path

class StrapFileResolver(
    private val translationStrategy: TranslationStrategy
) {

    fun resolve(path: Path): List<Entity> {
        val text = Files.readString(path)

        val lexer = Lexer(text)
        val tokens = lexer.lex()

        val parser = Parser(text, tokens)
        val ast = parser.parse()

        val interpreter = Interpreter(text, ast, translationStrategy)
        return interpreter.interpret()
    }
}

package com._7aske.strapparser

import com._7aske.strapparser.parser.Interpreter
import com._7aske.strapparser.parser.Lexer
import com._7aske.strapparser.parser.Parser
import java.nio.file.Files
import java.nio.file.Paths

fun main(args: Array<String>) {
    val text = Files.readString(Paths.get(args[0]))

    val lexer = Lexer(text)
    val tokens = lexer.lex()

    val parser = Parser(text, tokens)
    val ast = parser.parse()

    val interpreter = Interpreter(text, ast)
    val entities = interpreter.interpret()

    entities.forEach { println(it) }
}
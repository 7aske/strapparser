package com._7aske.strapparser

import com._7aske.strapparser.parser.Lexer
import com._7aske.strapparser.parser.Parser
import java.nio.file.Files
import java.nio.file.Paths

fun main(args: Array<String>) {
    val string = Files.readString(Paths.get("./test.strap"))

    val lexer = Lexer(string)
    val tokens = lexer.lex()

    val parser = Parser(string, tokens)
    val ast = parser.parse()

    ast.forEach { println(it) }
}
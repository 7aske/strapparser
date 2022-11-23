package com._7aske.strapparser.parser.iter

import com._7aske.strapparser.parser.Token
import com._7aske.strapparser.parser.TokenType

open class TokenIterator(private val tokens: List<Token>) : Iterator<Token> {
    private val endIndex = tokens.size
    var index = 0

    override fun hasNext(): Boolean {
        return index < endIndex
    }

    override fun next(): Token {
        if (hasNext()) return tokens[index++]
        throw NoSuchElementException()
    }

    @JvmOverloads
    fun rewind(num: Int = 1) {
        if (index - num < 0) throw IndexOutOfBoundsException("Index out of bounds: ${index - num}")
        index -= num
    }

    fun prev(): Token {
        if (index > 0) return tokens[index - 1]
        throw NoSuchElementException()
    }

    fun peek(): Token {
        if (hasNext()) return tokens[index]
        throw NoSuchElementException()
    }

    fun eatWhile(predicate: (token: Token) -> Boolean): List<Token> {
        val eaten: MutableList<Token> = ArrayList()
        while (hasNext() && predicate(peek())) eaten.add(next())
        return eaten
    }

    fun isPeekOfType(vararg types: TokenType): Boolean {
        return hasNext() && types.contains(peek().type)
    }
}

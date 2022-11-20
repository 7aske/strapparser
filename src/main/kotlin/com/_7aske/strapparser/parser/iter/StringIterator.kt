package com._7aske.strapparser.parser.iter


open class StringIterator(private val content: String) : Iterator<Char> {
    private val endIndex: Int = content.length
    private var index = 0

    override fun hasNext(): Boolean {
        return index < endIndex
    }

    override fun next(): Char {
        if (hasNext()) return content[index++]
        throw NoSuchElementException()
    }

    @JvmOverloads
    fun rewind(num: Int = 1) {
        if (index - num < 0) throw IndexOutOfBoundsException("Cannot rewind $num")
        index -= num
    }

    fun prev(): Char {
        if (index > 0) return content[index - 1]
        throw NoSuchElementException()
    }

    fun peek(): Char {
        if (hasNext()) return content[index]
        throw NoSuchElementException()
    }

    fun isPeek(value: Char): Boolean {
        return if (hasNext())
            content[index] == value
        else
            false
    }

    fun eatWhile(predicate: (str: Char) -> Boolean): String {
        val builder = StringBuilder()
        while (hasNext() && predicate(peek())) builder.append(next())
        return builder.toString()
    }

    fun eatWhitespace(): String {
        return eatWhile { it.isWhitespace() }
    }

    fun eatSpace(): String {
        return eatWhile { it == ' ' }
    }

    fun eatWord(): String {
        return eatWhile { it.isLetterOrDigit() }
    }

    fun eatFloat(): String {
        val builder = StringBuilder()
        do {
            if (hasNext()) builder.append(next()) else return builder.toString()
        } while (builder.toString().matches("^([+-]?\\d+\\.?\\d*)$".toRegex()))
        rewind()
        builder.setLength(builder.length - 1)
        return builder.toString()
    }
}

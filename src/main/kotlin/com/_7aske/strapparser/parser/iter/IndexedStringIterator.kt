package com._7aske.strapparser.parser.iter

open class IndexedStringIterator(content: String) : StringIterator(content) {
    protected var char = 0
    protected var row = 0

    override fun next(): Char {
        val value = super.next()
        if (value == '\n') {
            row++
            char = 0
        } else {
            char++
        }
        return value
    }
}

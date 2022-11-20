package com._7aske.strapparser.parser.iter

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class StringIteratorTest {
    lateinit var iter: StringIterator

    @Test
    fun `test regular characters`() {
        val text = "1 2 3 4"

        iter = StringIterator(text)

        Assertions.assertEquals('1', iter.next())
        Assertions.assertEquals(' ', iter.next())
        Assertions.assertEquals('2', iter.next())
        Assertions.assertEquals(' ', iter.next())
        Assertions.assertEquals('3', iter.next())
        Assertions.assertEquals(' ', iter.next())
        Assertions.assertEquals('4', iter.next())
    }

    @Test

    fun `test rewind`() {
        val text = "1 2 3 4"

        iter = StringIterator(text)

        Assertions.assertEquals('1', iter.next())
        iter.rewind()
        Assertions.assertEquals('1', iter.next())
    }
}
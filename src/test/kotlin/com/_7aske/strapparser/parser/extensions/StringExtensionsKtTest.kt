package com._7aske.strapparser.parser.extensions

import com._7aske.strapparser.extensions.ordinalIndexOf
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class StringExtensionsKtTest {

    @Test
    fun `test ordinal index`() {
        val text = "1 1 1 1 1 1"

        assertEquals(4, text.ordinalIndexOf("1", 3))
    }
}

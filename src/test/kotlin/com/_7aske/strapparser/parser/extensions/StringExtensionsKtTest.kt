package com._7aske.strapparser.parser.extensions

import com._7aske.strapparser.extensions.ordinalIndexOf
import com._7aske.strapparser.extensions.toKebabCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class StringExtensionsKtTest {

    @Test
    fun `test ordinal index`() {
        val text = "1 1 1 1 1 1"

        assertEquals(4, text.ordinalIndexOf("1", 3))
    }

    @Test
    fun `test kebab case`() {
        val text = "sabbraCadabra"

        assertEquals("sabbra-cadabra", text.toKebabCase())
    }
}

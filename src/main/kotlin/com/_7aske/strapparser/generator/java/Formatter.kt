package com._7aske.strapparser.generator.java

import com.google.googlejavaformat.java.Formatter

class Formatter {
    private val googleFormatter = Formatter()

    fun formatSource(source: String): String {
        return googleFormatter.formatSource(source)
    }
}

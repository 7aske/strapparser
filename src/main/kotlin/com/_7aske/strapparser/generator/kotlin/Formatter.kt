package com._7aske.strapparser.generator.kotlin

import com.facebook.ktfmt.format.Formatter

class Formatter {

    fun formatSource(source: String): String {
        return Formatter.format(source, true)
    }
}

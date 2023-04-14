package com._7aske.strapparser.util

import com._7aske.strapparser.extensions.capitalize
import com._7aske.strapparser.extensions.ordinalIndexOf
import com._7aske.strapparser.parser.Token

const val TAB_OFFSET = 3

/**
 * Prints the location of the token in the text.
 *
 * @param text the where the token is located.
 * @param row: row where token is located (zero-indexed)
 */
fun printLocation(
    text: String,
    row: Int,
    startChar: Int,
    endChar: Int
) {
    // + 1 to skip that starting newline
    val startIndex = text.ordinalIndexOf("\n", row)
    // to find the end of the line
    val endIndex = text.indexOf("\n", startIndex + 1).let {
        if (it == -1) {
            text.length
        } else {
            it
        }
    }

    val line = text.substring(startIndex, endIndex)
    System.err.println(line)

    val numTabs = line.count { it == '\t' }

    repeat(startChar + TAB_OFFSET * numTabs) {
        System.err.print(' ')
    }
    repeat(endChar - startChar) {
        System.err.print('^')
    }
    System.err.print('\n')

    repeat(startChar + TAB_OFFSET * numTabs) {
        System.err.print('─')
    }

    System.err.print('┘')
    System.err.print('\n')
}

fun printLocation(text: String, token: Token) =
    printLocation(text, token.startRow , token.startChar, token.endChar)


fun snakeCaseToCamelCase(text: String) =
    text.split("_").joinToString("") { it.capitalize() }
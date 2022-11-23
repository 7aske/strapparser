package com._7aske.strapparser.util

import com._7aske.strapparser.extensions.ordinalIndexOf
import com._7aske.strapparser.parser.Token

const val TAB_OFFSET = 3

class ParserUtil private constructor() {
    companion object {
        fun printLocation(text: String, row: Int, startChar: Int, endChar: Int) {
            // + 1 to skip that starting newline
            val startIndex = text.ordinalIndexOf("\n", row) + 1
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
            printLocation(text, token.startRow, token.startChar, token.endChar)
    }
}

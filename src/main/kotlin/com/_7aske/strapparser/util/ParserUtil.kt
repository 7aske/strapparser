package com._7aske.strapparser.util

import com._7aske.strapparser.parser.Token
import com._7aske.strapparser.parser.extensions.ordinalIndexOf

class ParserUtil private constructor() {
    companion object {
        fun printLocation(text: String, token: Token) {
            // + 1 to skip that starting newline
            val startIndex = text.ordinalIndexOf("\n", token.startRow) + 1
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

            for (i in 0 until token.startChar + 3 * numTabs) {
                System.err.print(' ')
            }
            for (i in 0 until token.endChar - token.startChar) {
                System.err.print('^')
            }
            System.err.print('\n')

            for (i in 0 until token.startChar + 3 * numTabs) {
                System.err.print('─')
            }
            System.err.print('┘')
            System.err.print('\n')
        }
    }
}
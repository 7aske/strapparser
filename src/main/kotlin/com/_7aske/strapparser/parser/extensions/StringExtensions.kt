package com._7aske.strapparser.parser.extensions

fun String.ordinalIndexOf(str: String, ordinal: Int): Int {
    var n = ordinal

    var pos = this.indexOf(str)

    while(--n > 0 && pos != -1) {
        pos = this.indexOf(str, pos + 1)
    }

    return pos
}

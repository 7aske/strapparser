package com._7aske.strapparser.extensions

fun String.ordinalIndexOf(str: String, ordinal: Int): Int {
    var n = ordinal

    var pos = this.indexOf(str)

    while (--n > 0 && pos != -1) {
        pos = this.indexOf(str, pos + 1)
    }

    return pos
}

fun String.plural(): String {
    if (this.endsWith("s")) return this + "es"
    if (this.endsWith("x")) return this + "es"
    if (this.endsWith("y")) return this.substring(0, this.length - 1) + "ies"
    return this + "s"
}

fun String.capitalize(): String = this.replaceFirstChar {
    it.uppercase()
}

fun String.uncapitalize(): String = this.replaceFirstChar {
    it.lowercase()
}

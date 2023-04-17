package com._7aske.strapparser.generator.kotlin

fun reverseLastTwoParameters(params: List<String>): List<String> {
    val copy = params.toMutableList()
    if (copy.size < 2) return copy
    val last = copy.last()
    val secondLast = copy[copy.size - 2]
    copy[copy.size - 2] = last
    copy[copy.size - 1] = secondLast
    return copy
}

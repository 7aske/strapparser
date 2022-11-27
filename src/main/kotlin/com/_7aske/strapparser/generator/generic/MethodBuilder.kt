package com._7aske.strapparser.generator.generic

interface MethodBuilder {
    var modifier: String
    var returnType: String
    var implementation: String
    var parameters: MutableList<List<String>>
    val name: String
    val annotations: MutableList<String>
    val throws: MutableList<String>

    fun build(): String
}
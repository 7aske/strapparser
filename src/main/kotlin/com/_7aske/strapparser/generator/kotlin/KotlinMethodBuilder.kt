package com._7aske.strapparser.generator.kotlin

import com._7aske.strapparser.extensions.capitalize
import com._7aske.strapparser.generator.generic.MethodBuilder

class KotlinMethodBuilder(override val name: String) : MethodBuilder {
    override var modifier: String = ""
    override var abstract: Boolean = false
    var override: Boolean = false
    override var returnType: String = ""
    override var implementation: String =
        "TODO(\"Not yet implemented\")"
    override var parameters: MutableList<List<String>> = mutableListOf()
    override val annotations: MutableList<String> = mutableListOf()
    override val throws: MutableList<String> = mutableListOf()

    companion object {
        fun abstract(name: String) = KotlinMethodBuilder(name).apply {
            abstract = true
            modifier = ""
        }

        fun of(name: String) = KotlinMethodBuilder(name)

        fun setter(field: String, type: String): KotlinMethodBuilder {
            val name = "set" + field.capitalize()
            return KotlinMethodBuilder(name).apply {
                parameters.add(listOf(type, field))
                implementation = "this.$field = $field;"
            }
        }

        fun getter(field: String, type: String): KotlinMethodBuilder {
            val name = "get" + field.capitalize()
            return KotlinMethodBuilder(name).apply {
                returnType = type
                implementation = "return this.$field;"
            }
        }
    }

    override fun toString(): String {
        return build()
    }

    override fun build(): String =
        buildString {
            appendLine(annotations.joinToString("\n"))
            append(modifier)
            append(" ")
            if (override) {
                append("override ")
            }
            append("fun ")
            append(name)
            append("(")
            append(
                parameters.joinToString(", ") {
                    val params = reverseLastTwoParameters(it)
                    val annotations = params.take(params.size - 2)
                    val rest = params.takeLast(2)
                    annotations.joinToString(" ") + " " + rest.joinToString(":")
                }
            )
            append(")")
            if (returnType.isNotEmpty()) {
                append(": ")
                append(returnType)
            }

            if (!abstract) {
                append("{")
                append(implementation)
                append("}")
            }
        }
}

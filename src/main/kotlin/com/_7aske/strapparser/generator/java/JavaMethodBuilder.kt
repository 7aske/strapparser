package com._7aske.strapparser.generator.java

import com._7aske.strapparser.extensions.capitalize
import com._7aske.strapparser.generator.generic.MethodBuilder

const val OVERRIDE = "@Override"

class JavaMethodBuilder(override val name: String) : MethodBuilder {
    override var modifier: String = "public"
    override var returnType: String = "void"
    override var implementation: String =
        "throw new IllegalStateException(\"Not implemented\");"
    override var parameters: MutableList<List<String>> = mutableListOf()
    override val annotations: MutableList<String> = mutableListOf()
    override val throws: MutableList<String> = mutableListOf()

    companion object {
        fun of(name: String) = JavaMethodBuilder(name)

        fun setter(field: String, type: String): MethodBuilder {
            val name = "set" + field.capitalize()
            return JavaMethodBuilder(name).apply {
                parameters.add(listOf(type, field))
                implementation = "this.$field = $field;"
            }
        }

        fun getter(field: String, type: String): MethodBuilder {
            val name = "get" + field.capitalize()
            return JavaMethodBuilder(name).apply {
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
            append(annotations.joinToString("\n"))
                .append("\n")
            append(modifier)
                .append(" ")
                .append(returnType)
                .append(" ")
                .append(name)
                .append("(")
                .append(parameters.joinToString(", ") { it.joinToString(" ") })
                .append(")")
            if (throws.isNotEmpty()) {
                append(" throws ")
                append(throws.joinToString(", "))
            }
            append("{")
            append(implementation)
            append("}")
        }
}

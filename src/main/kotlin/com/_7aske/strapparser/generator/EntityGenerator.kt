package com._7aske.strapparser.generator

import com._7aske.strapparser.parser.definitions.Field
import java.nio.file.Path

interface EntityGenerator {

    fun getOutputFilePath(): Path

    fun generateEntity(): String

    fun generateSetter(field: Field): String

    fun generateGetter(field: Field): String

    fun resolveClassName(): String

    fun resolveVariableName(): String

    fun resolveVariableName(field: Field): String

    fun resolveFieldSetter(field: Field): String

    fun resolveFieldGetter(field: Field): String

    fun generateField(field: Field, ctx: GeneratorContext): String
}

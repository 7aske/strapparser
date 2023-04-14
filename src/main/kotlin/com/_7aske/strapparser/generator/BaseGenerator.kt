package com._7aske.strapparser.generator

import java.nio.file.FileSystems
import java.nio.file.Path

abstract class BaseGenerator(
    protected val ctx: GeneratorContext,
    protected val dataTypeResolver: DataTypeResolver
) {
    protected val separator = FileSystems.getDefault().separator!!

    abstract fun getOutputFilePath(): Path

    abstract fun generate(): String
}

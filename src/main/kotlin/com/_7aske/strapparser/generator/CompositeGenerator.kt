package com._7aske.strapparser.generator

abstract class CompositeGenerator(
    protected val ctx: GeneratorContext,
    protected val dataTypeResolver: DataTypeResolver
) {
    abstract fun generate()
}

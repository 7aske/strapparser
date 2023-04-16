package com._7aske.strapparser.generator

import com._7aske.strapparser.generator.java.JavaClassGenerator

abstract class ControllerGenerator(
    protected val service: ServiceGenerator,
    ctx: GeneratorContext,
    dataTypeResolver: DataTypeResolver
) : JavaClassGenerator(ctx, dataTypeResolver) {

    abstract fun generateEndpoints(): String

    abstract fun resolveEndpoint(): String

    abstract fun getEntityGenerator(): EntityGenerator
    abstract fun resolveIdFieldsParameters(): List<List<String>>
}

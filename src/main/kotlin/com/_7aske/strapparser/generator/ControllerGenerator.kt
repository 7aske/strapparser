package com._7aske.strapparser.generator

abstract class ControllerGenerator(
    protected val service: ServiceGenerator,
    ctx: GeneratorContext,
    dataTypeResolver: DataTypeResolver
) : BaseGenerator(ctx, dataTypeResolver) {

    abstract fun generateEndpoints(): String

    abstract fun resolveEndpoint(): String
}

package com._7aske.strapparser.generator

abstract class ControllerGenerator(
    protected val service: ServiceGenerator,
    ctx: GeneratorContext,
    dataTypeResolver: DataTypeResolver
) : BaseGenerator(ctx, dataTypeResolver)

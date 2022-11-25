package com._7aske.strapparser.generator

abstract class ServiceGenerator(
    protected val entity: EntityGenerator,
    ctx: GeneratorContext,
    dataTypeResolver: DataTypeResolver
) : BaseGenerator(ctx, dataTypeResolver)

package com._7aske.strapparser.generator

abstract class ServiceImplGenerator(
    protected val entity: EntityGenerator,
    ctx: GeneratorContext,
    dataTypeResolver: DataTypeResolver
) : BaseGenerator(ctx, dataTypeResolver)

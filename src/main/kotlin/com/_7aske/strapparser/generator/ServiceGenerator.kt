package com._7aske.strapparser.generator

abstract class ServiceGenerator(
    val entity: EntityGenerator,
    ctx: GeneratorContext,
    dataTypeResolver: DataTypeResolver
) : BaseGenerator(ctx, dataTypeResolver)

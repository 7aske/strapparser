package com._7aske.strapparser.generator

abstract class RootGenerator(
    ctx: GeneratorContext,
    dataTypeResolver: DataTypeResolver
) : CompositeGenerator(ctx, dataTypeResolver)
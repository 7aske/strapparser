package com._7aske.strapparser.generator

import com._7aske.strapparser.generator.java.JavaClassGenerator

abstract class ServiceImplGenerator(
    protected val entity: EntityGenerator,
    ctx: GeneratorContext,
    dataTypeResolver: DataTypeResolver
) : JavaClassGenerator(ctx, dataTypeResolver)

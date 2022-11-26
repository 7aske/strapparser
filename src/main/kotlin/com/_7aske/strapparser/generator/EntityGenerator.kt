package com._7aske.strapparser.generator

import com._7aske.strapparser.parser.definitions.Entity
import com._7aske.strapparser.parser.definitions.Field

abstract class EntityGenerator(
    protected val entity: Entity,
    ctx: GeneratorContext,
    dataTypeResolver: DataTypeResolver
) : BaseGenerator(ctx, dataTypeResolver) {

    abstract fun generateSetter(field: Field): String

    abstract fun generateGetter(field: Field): String

    abstract fun resolveVariableName(field: Field): String

    abstract fun generateField(field: Field): String

    abstract fun getIdFields(): List<Field>

    abstract fun resolveIdFieldPathVariables(): String

    abstract fun resolveIdFieldVariables(): String

    abstract fun resolveIdFieldsParameters(): String
}

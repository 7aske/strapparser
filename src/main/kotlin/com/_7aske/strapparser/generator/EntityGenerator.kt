package com._7aske.strapparser.generator

import com._7aske.strapparser.parser.definitions.Entity
import com._7aske.strapparser.parser.definitions.Field

abstract class EntityGenerator(
    internal val entity: Entity,
    ctx: GeneratorContext,
    dataTypeResolver: DataTypeResolver
) : BaseGenerator(ctx, dataTypeResolver) {

    abstract fun generateSetter(field: Field): String

    abstract fun generateGetter(field: Field): String

    abstract fun getVariableName(field: Field): String

    abstract fun generateField(field: Field): String

    abstract fun getIdFields(): List<Field>

    abstract fun getIdFieldPathVariables(): String

    abstract fun getIdFieldVariables(): String

    abstract fun getIdFieldsAsArguments(): String

    abstract fun hasCompositeId(): Boolean

    abstract fun getIdClassName(): String

    abstract fun getIdFQCN(): String

    abstract fun getCompositeIdFieldVariables(): String
}

package com._7aske.strapparser.generator

import com._7aske.strapparser.cli.Args
import com._7aske.strapparser.parser.Token
import com._7aske.strapparser.parser.TokenType
import com._7aske.strapparser.parser.definitions.Entity
import com._7aske.strapparser.parser.definitions.Field
import com._7aske.strapparser.parser.definitions.ListFieldType
import com._7aske.strapparser.parser.definitions.RefFieldType

class GeneratorContext(
    private val entities: Map<String, Entity>,
    internal val args: Args,
    internal val dependencies: MutableCollection<String> = mutableSetOf(),
) {
    fun getReferencedEntity(ref: RefFieldType): Entity? =
        entities[ref.value]

    fun getReferencedEntity(ref: String): Entity? =
        entities[ref]

    fun getOutputLocation() =
        args.output.replaceFirst("~", System.getProperty("user.home"))

    fun getPackageName(vararg packages: String): String {
        val parts = args.domain.split(".").toMutableList()

        if (args.name.isNotBlank()) {
            parts.add(args.name)
        }

        parts.addAll(packages)

        return parts.joinToString(".")
    }

    fun getEntityFieldsThatReference(entityName: String): List<Field> {
        return entities.values
            .filter { it.name != entityName }
            .filter {
                it.fields.any { field ->
                    field.isRef() && field.getReferencedEntityName() == entityName
                }
            }.map { toField(it) }
            .toList()
    }

    private fun toField(entity: Entity): Field {
        val token = Token(TokenType.TYPE, entity.name, -1, -1, -1)
        val fieldToken = Token(TokenType.FIELD, "field", -1, -1, -1)
        val type = ListFieldType(token, entity.name)

        return Field(
            fieldToken,
            entity.name.replaceFirstChar { it.lowercase() },
            type,
            listOf()
        )
    }
}

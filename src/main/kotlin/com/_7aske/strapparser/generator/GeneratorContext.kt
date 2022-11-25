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
    internal val args: Args
) {
    fun getReferencedEntity(ref: RefFieldType): Entity? =
        entities[ref.value]

    fun getOutputLocation() =
        args.output.replaceFirst("~", System.getProperty("user.home"))

    fun getPackageName(vararg packages: String): String {
        if (args.domain.isEmpty()) {
            return packages.joinToString(".")
        }

        return args.domain + "." + packages.joinToString(".")
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

package com._7aske.strapparser.parser

import com._7aske.strapparser.extensions.uncapitalize
import com._7aske.strapparser.parser.ast.*
import com._7aske.strapparser.parser.definitions.*
import com._7aske.strapparser.util.printLocation

class Interpreter(
    private val text: String,
    private val astList: List<AstNode>
) {
    // interpretation result
    private val entities = mutableMapOf<String, Entity>()
    private val incompleteRefs = mutableListOf<FieldType>()

    fun interpret(): List<Entity> {
        for (ast in astList) {
            evaluate(ast)
        }

        evaluateIncompleteRefs()

        return entities.values.toList()
    }

    private fun evaluateIncompleteRefs() {
        // this can be optimized
        for (entity in entities.values) {
            val resolvedFields = entity.fields
                .map {
                    if (it.type is IncompleteRefFieldType) {
                        check(entities.contains(it.type.value)) {
                            printLocation(text, it.type.token)
                            "Undefined reference to entity '${it.type.value}'"
                        }

                        it.type = RefFieldType(
                            it.token,
                            it.type.value,
                        )
                    }

                    if (it.type is IncompleteListFieldType) {
                        check(entities.contains(it.type.value)) {
                            printLocation(text, it.type.token)
                            "Undefined reference to entity '${it.type.value}'"
                        }

                        it.type = ListFieldType(
                            it.token,
                            it.type.value,
                        )
                    }

                    it
                }

            entity.fields = resolvedFields
        }
    }

    private fun evaluate(ast: AstNode) {
        when (ast) {
            is AstEntityNode -> {
                val entity = evaluateEntityNode(ast)
                entities[entity.name] = entity
            }
        }
    }

    private fun evaluateEntityNode(ast: AstEntityNode): Entity {
        val name = ast.name.token.value
        val fields = ast.fields
            .map { evaluateFieldNode(it) }
            .toList()

        val attrs = ast.attributes
            .map { Attribute(it.token, (it as AstAttributeNode).getValue()) }

        return Entity(ast.token, name, fields, attrs)
    }

    private fun evaluateFieldNode(ast: AstFieldNode): Field {
        val name = ast.name.token.value.uncapitalize()
        val type = evaluateAstTypeNode(ast.type)

        val attrs = ast.attributes
            .map { Attribute(it.token, (it as AstAttributeNode).getValue()) }

        return Field(ast.token, name, type, attrs)
    }

    private fun evaluateAstTypeNode(type: AstNode): FieldType {
        return when (type) {
            is AstRefNode -> {
                val reference = type.typeNode.token.value
                if (entities.contains(reference)) {
                    return RefFieldType(
                        type.typeNode.token,
                        reference,
                    )
                } else {
                    val incompleteType = IncompleteRefFieldType(
                        type.typeNode.token,
                        reference
                    )

                    incompleteRefs.add(incompleteType)

                    return incompleteType
                }
            }

            is AstListNode -> {
                val reference = type.typeNode.token.value
                if (entities.contains(reference)) {
                    return ListFieldType(
                        type.typeNode.token,
                        reference,
                    )
                } else {
                    val incompleteType = IncompleteListFieldType(
                        type.typeNode.token,
                        reference
                    )

                    incompleteRefs.add(incompleteType)

                    return incompleteType
                }
            }

            is AstIdentifierNode -> DataFieldType(type.token, type.token.value)
            else -> {
                printLocation(text, type.token)
                error("Invalid field type declaration")
            }
        }
    }
}

package com._7aske.strapparser.parser

import com._7aske.strapparser.parser.ast.*
import com._7aske.strapparser.parser.iter.TokenIterator
import com._7aske.strapparser.util.ParserUtil.Companion.printLocation

class Parser(private val text: String, tokens: List<Token>) :
    TokenIterator(tokens) {
    private val parsed = mutableListOf<AstNode>()

    fun parse(): MutableList<AstNode> {
        while (hasNext()) {
            skip(TokenType.NEWLINE, TokenType.TAB, TokenType.SPACE)

            if (isPeekOfType(TokenType.ENTITY)) {
                parsed.add(parseEntityNode())
                continue
            }

            printLocation(text, peek())
            throw unexpectedToken(peek())
        }

        return parsed
    }

    private fun skip(tokenType: TokenType, vararg tokenTypes: TokenType) {
        eatWhile { mutableListOf(tokenType, *tokenTypes).contains(it.type) }
    }

    private fun parseEntityNode(): AstEntityNode {
        val entityToken = getOrThrowExpectedToken(TokenType.ENTITY)

        val identifierToken = getOrThrowExpectedToken(TokenType.IDENTIFIER)
        val identifierNode = AstIdentifierNode(identifierToken)

        getOrThrowUnexpectedToken(TokenType.NEWLINE)

        val fields = mutableListOf<AstFieldNode>()

        while (isPeekOfType(TokenType.TAB, TokenType.SPACE, TokenType.FIELD)) {
            skip(TokenType.TAB, TokenType.SPACE)
            fields.add(parseFieldNode())
            skip(TokenType.NEWLINE)
        }

        val attributeNodes = mutableListOf<AstNode>()
        while (isPeekOfTypeAttribute())
            attributeNodes.add(AstAttributeNode(next()))

        return AstEntityNode(
            entityToken,
            identifierNode,
            fields,
            attributeNodes
        )
    }

    private fun isPeekOfTypeAttribute(): Boolean =
        isPeekOfType(*TokenType.attributeTypes)

    private fun parseFieldNode(): AstFieldNode {
        val fieldToken = getOrThrowExpectedToken(TokenType.FIELD)

        val identifierToken = getOrThrowExpectedToken(TokenType.IDENTIFIER)

        val identifierNode = AstIdentifierNode(identifierToken)

        val type = if (isPeekOfType(TokenType.REFERENCES)) {
            parseRefNode()
        } else if (isPeekOfType(TokenType.LIST)) {
            parseListNode()
        } else {
            parseTypeNode()
        }

        val attributeNodes = mutableListOf<AstNode>()
        while (isPeekOfTypeAttribute())
            attributeNodes.add(AstAttributeNode(next()))

        return AstFieldNode(fieldToken, identifierNode, type, attributeNodes)
    }

    private fun parseListNode(): AstListNode {
        val refToken = getOrThrowExpectedToken(TokenType.LIST)

        val typeNode = parseTypeNode()

        return AstListNode(refToken, typeNode)
    }

    private fun parseRefNode(): AstRefNode {
        val refToken = getOrThrowExpectedToken(TokenType.REFERENCES)

        val typeNode = parseTypeNode()

        return AstRefNode(refToken, typeNode)
    }

    private fun parseTypeNode(): AstIdentifierNode {
        val typeToken = getOrThrowExpectedToken(TokenType.IDENTIFIER)
        return AstIdentifierNode(typeToken)
    }

    private fun getNextIfType(type: TokenType): Token? =
        if (isPeekOfType(type)) {
            next()
        } else {
            null
        }

    private fun getOrThrowExpectedToken(expected: TokenType): Token {
        val token = getNextIfType(expected)

        if (token == null) {
            printLocation(text, peek())
            throw expectedToken(expected, peek())
        }

        return token
    }

    private fun getOrThrowUnexpectedToken(expected: TokenType): Token {
        val token = getNextIfType(expected)

        if (token == null) {
            printLocation(text, peek())
            throw unexpectedToken(peek())
        }

        return token
    }

    private fun unexpectedToken(token: Token) =
        IllegalStateException("Unexpected token type ${token.type}")

    private fun expectedToken(expected: TokenType, got: Token) =
        IllegalStateException("Expected token type $expected got ${got.type}")
}

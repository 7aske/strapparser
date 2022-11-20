package com._7aske.strapparser.parser.definitions

import com._7aske.strapparser.parser.Token

open class FieldType(
    val token: Token,
    val value: String
)

class IncompleteRefFieldType(token: Token, value: String) : FieldType(token, value)

class RefFieldType(token: Token, value: String, val reference: Entity) : FieldType(token, value)

class ListFieldType(token: Token, value: String) : FieldType(token, value)

class DataFieldType(token: Token, value: String) : FieldType(token, value)

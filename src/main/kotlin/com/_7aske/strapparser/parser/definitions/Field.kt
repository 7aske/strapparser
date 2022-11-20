package com._7aske.strapparser.parser.definitions

import com._7aske.strapparser.parser.Token

class Field(
    val token: Token,
    val name: String,
    var type: FieldType,
    val attributes: List<Attribute>
) {
}
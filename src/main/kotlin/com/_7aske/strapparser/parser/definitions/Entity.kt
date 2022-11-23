package com._7aske.strapparser.parser.definitions

import com._7aske.strapparser.parser.Token

class Entity(
    token: Token,
    val name: String,
    var fields: List<Field>,
    val attributes: List<Attribute>
) : Definition(token)

package com._7aske.strapparser.parser.definitions

import com._7aske.strapparser.parser.Token

open class Attribute(token: Token, val value: String) : Definition(token)

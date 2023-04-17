package com._7aske.strapparser.generator.spring

import com._7aske.strapparser.generator.EntityGenerator

interface SpringControllerGenerator {
    fun generateEndpoints(): String

    fun resolveEndpoint(): String

    fun getEntityGenerator(): EntityGenerator
    fun resolveIdFieldsParameters(): List<List<String>>
}

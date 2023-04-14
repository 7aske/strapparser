package com._7aske.strapparser.generator.generic

abstract class Dependency(
    protected val name: String,
    protected val version: String?
) {
    abstract fun generate(): String
}
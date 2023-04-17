package com._7aske.strapparser.generator.spring

class Mapping private constructor(
    private val path: String,
    private val type: String,
) {
    companion object {
        fun request(path: String) = Mapping(path, "RequestMapping")
        fun get(path: String = "") = Mapping(path, "GetMapping")
        fun post(path: String = "") = Mapping(path, "PostMapping")
        fun put(path: String = "") = Mapping(path, "PutMapping")
        fun delete(path: String = "") = Mapping(path, "DeleteMapping")
    }

    override fun toString(): String {
        val prefix = "@$type"

        if (path.isEmpty())
            return prefix + "\n"

        return "$prefix(\"/$path\")\n"
    }
}

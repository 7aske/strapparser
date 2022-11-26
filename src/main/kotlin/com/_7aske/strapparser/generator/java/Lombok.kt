package com._7aske.strapparser.generator.java

class Lombok(private val annotation: String, private vararg val params: String) {
    companion object {
        val RequiredArgsConstructor = Lombok("RequiredArgsConstructor")
        val ToString = Lombok("ToString")
        val ToStringExclude = Lombok("ToString.Exclude")
        val Data = Lombok("Data")
        val EqualsAndHashCode = Lombok("EqualsAndHashCode", "onlyExplicitlyIncluded=true")
        val EqualsAndHashCodeInclude = Lombok("EqualsAndHashCode.Include")
    }
    override fun toString(): String = if (params.isEmpty()) {
            "@lombok.$annotation\n"
        } else {
            "@lombok.$annotation(${params.joinToString(", ")})\n"
        }
}
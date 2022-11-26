package com._7aske.strapparser.generator.java

class Lombok(
    private val annotation: String,
    private vararg val params: String
) {
    companion object {
        val Setter = Lombok("Setter")
        val Getter = Lombok("Getter")
        val RequiredArgsConstructor = Lombok("RequiredArgsConstructor")
        val NoArgsConstructor = Lombok("NoArgsConstructor")
        val ProtectedNoArgsConstructor =
            Lombok("NoArgsConstructor", "access = lombok.AccessLevel.PROTECTED")
        val AllArgsConstructor = Lombok("AllArgsConstructor")
        val ToString = Lombok("ToString")
        val ToStringExclude = Lombok("ToString.Exclude")
        val Data = Lombok("Data")
        val EqualsAndHashCode = Lombok(
            "EqualsAndHashCode",
            "onlyExplicitlyIncluded=true",
            "callSuper=false"
        )
        val EqualsAndHashCodeInclude = Lombok("EqualsAndHashCode.Include")
    }

    override fun toString(): String = if (params.isEmpty()) {
        "@lombok.$annotation\n"
    } else {
        "@lombok.$annotation(${params.joinToString(", ")})\n"
    }
}

package com._7aske.strapparser.generator.generic

class MavenDependency(
    name: String,
    version: String?
) : Dependency(name, version) {

    override fun generate(): String = buildString {
        appendLine("        <dependency>")
        appendLine("            <groupId>${name.substring(0, name.lastIndexOf("."))}</groupId>")
        appendLine("            <artifactId>${name.substring(name.lastIndexOf(".") + 1)}</artifactId>")
        if (version != null) {
            appendLine("            <version>$version</version>")
        }
        appendLine("        </dependency>")
    }
}

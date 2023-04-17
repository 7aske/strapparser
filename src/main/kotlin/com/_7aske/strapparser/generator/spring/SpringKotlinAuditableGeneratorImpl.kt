package com._7aske.strapparser.generator.spring

import com._7aske.strapparser.generator.DataTypeResolver
import com._7aske.strapparser.generator.GeneratorContext
import com._7aske.strapparser.generator.kotlin.Formatter
import com._7aske.strapparser.generator.kotlin.KotlinClassGenerator
import java.nio.file.Path
import java.nio.file.Paths

class SpringKotlinAuditableGeneratorImpl(
    ctx: GeneratorContext,
    dataTypeResolver: DataTypeResolver
) : KotlinClassGenerator(
    ctx, dataTypeResolver
) {
    private val formatter = Formatter()

    init {
        import("jakarta.persistence.*")
        import("org.springframework.data.jpa.domain.support.AuditingEntityListener")
        import("org.springframework.data.annotation.*")
        import("java.io.Serializable")
    }

    override fun getOutputFilePath(): Path = Paths.get(
        ctx.getOutputLocation(),
        "src",
        "main",
        "kotlin",
        getPackage().replace(".", separator),
        this.getClassName() + ".kt"
    )

    override fun generate(): String =
        formatter.formatSource(
            buildString {
                appendLine("package ${getPackage()}")

                appendLine(getImports())

                appendLine("@MappedSuperclass")
                appendLine(
                    "@EntityListeners" +
                        "(AuditingEntityListener::class)"
                )
                appendLine("abstract class ${getClassName()} protected constructor() {")
                appendLine("@CreatedDate\n")
                appendLine("open var createdDate: Instant? = null\n\n")
                appendLine("@CreatedBy\n")
                appendLine("open var createdBy: String? = null\n\n")
                appendLine("@LastModifiedDate\n")
                appendLine("open var lastModifiedDate: Instant? = null\n\n")
                appendLine("@LastModifiedBy\n")
                appendLine("open var lastModifiedBy: String? = null\n\n")
                appendLine("open var recordStatus: Int? = 1\n\n")

                appendLine("}")
            }
        )

    override fun getClassName(): String = "Auditable"

    override fun getPackage(): String =
        ctx.getPackageName("entity")
}

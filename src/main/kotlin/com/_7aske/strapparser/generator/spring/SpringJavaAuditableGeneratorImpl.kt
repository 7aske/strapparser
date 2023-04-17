package com._7aske.strapparser.generator.spring

import com._7aske.strapparser.generator.DataTypeResolver
import com._7aske.strapparser.generator.GeneratorContext
import com._7aske.strapparser.generator.java.Formatter
import com._7aske.strapparser.generator.java.JavaClassGenerator
import com._7aske.strapparser.generator.java.JavaMethodBuilder
import com._7aske.strapparser.generator.java.Lombok
import java.nio.file.Path
import java.nio.file.Paths

class SpringJavaAuditableGeneratorImpl(
    ctx: GeneratorContext,
    dataTypeResolver: DataTypeResolver
) : JavaClassGenerator(
    ctx, dataTypeResolver
) {
    private val formatter = Formatter()

    init {
        import("jakarta.persistence.*")
        import("org.springframework.data.jpa.domain.support.AuditingEntityListener")
        import("org.springframework.data.annotation.*")
        import("java.io.Serializable")
        if (ctx.args.lombok) {
            import("lombok.*")
        }
    }

    override fun getOutputFilePath(): Path = Paths.get(
        ctx.getOutputLocation(),
        "src",
        "main",
        "java",
        getPackage().replace(".", separator),
        this.getClassName() + ".java"
    )

    override fun generate(): String =
        formatter.formatSource(
            buildString {
                append("package ${getPackage()};")
                append(getImports())

                if (ctx.args.lombok) {
                    append(Lombok.Getter)
                    append(Lombok.Setter)
                    append(Lombok.ProtectedNoArgsConstructor)
                }

                append("@MappedSuperclass")
                append(
                    "@EntityListeners" +
                        "(AuditingEntityListener.class)"
                )
                append("public abstract class ${getClassName()} implements Serializable {")
                append("@CreatedDate\n")
                append("private Instant createdDate;\n\n")
                append("@CreatedBy\n")
                append("private String createdBy;\n\n")
                append("@LastModifiedDate\n")
                append("private Instant lastModifiedDate;\n\n")
                append("@LastModifiedBy\n")
                append("private String lastModifiedBy;\n\n")
                append("private Integer recordStatus = 1;\n\n")

                if (!ctx.args.lombok) {
                    append("protected ${getClassName()}() {}")
                    append(getGettersAndSetters())
                }

                append("}")
            }
        )

    private fun getGettersAndSetters(): String {
        return buildString {
            append(JavaMethodBuilder.setter("createdDate", "Instant"))
            append(JavaMethodBuilder.getter("createdDate", "Instant"))
            append(JavaMethodBuilder.setter("lastModifiedDate", "Instant"))
            append(JavaMethodBuilder.getter("lastModifiedDate", "Instant"))
            append(JavaMethodBuilder.setter("createdBy", "String"))
            append(JavaMethodBuilder.getter("createdBy", "String"))
            append(JavaMethodBuilder.setter("lastModifiedBy", "String"))
            append(JavaMethodBuilder.getter("lastModifiedBy", "String"))
            append(JavaMethodBuilder.setter("recordStatus", "Integer"))
            append(JavaMethodBuilder.getter("recordStatus", "Integer"))
        }
    }

    override fun getClassName(): String = "Auditable"

    override fun getPackage(): String =
        ctx.getPackageName("entity")
}

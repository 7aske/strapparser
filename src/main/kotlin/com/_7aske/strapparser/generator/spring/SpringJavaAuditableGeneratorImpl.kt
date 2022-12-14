package com._7aske.strapparser.generator.spring

import com._7aske.strapparser.extensions.uncapitalize
import com._7aske.strapparser.generator.BaseGenerator
import com._7aske.strapparser.generator.DataTypeResolver
import com._7aske.strapparser.generator.GeneratorContext
import com._7aske.strapparser.generator.java.JavaMethodBuilder
import com._7aske.strapparser.generator.java.Lombok
import com.google.googlejavaformat.java.Formatter
import java.nio.file.Path
import java.nio.file.Paths

class SpringJavaAuditableGeneratorImpl(
    ctx: GeneratorContext,
    dataTypeResolver: DataTypeResolver
) : BaseGenerator(
    ctx, dataTypeResolver
) {
    private val formatter = Formatter()

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
                if (ctx.args.lombok) {
                    append(Lombok.Getter)
                    append(Lombok.Setter)
                    append(Lombok.ProtectedNoArgsConstructor)
                }
                append("@jakarta.persistence.MappedSuperclass")
                append(
                    "@jakarta.persistence.EntityListeners" +
                        "(org.springframework.data.jpa.domain.support.AuditingEntityListener.class)"
                )
                append("public abstract class ${getClassName()} implements java.io.Serializable {")
                append("@org.springframework.data.annotation.CreatedDate\n")
                append("private java.time.Instant createdDate;")
                append("@org.springframework.data.annotation.CreatedBy\n")
                append("private String createdBy;")
                append("@org.springframework.data.annotation.LastModifiedDate\n")
                append("private java.time.Instant lastModifiedDate;")
                append("@org.springframework.data.annotation.LastModifiedBy\n")
                append("private String lastModifiedBy;")
                append("private Integer recordStatus = 1;")

                if (!ctx.args.lombok) {
                    append("protected ${getClassName()}() {}")
                    append(getGettersAndSetters())
                }

                append("}")
            }
        )

    private fun getGettersAndSetters(): String {
        return buildString {
            append(JavaMethodBuilder.setter("createdDate", "java.time.Instant"))
            append(JavaMethodBuilder.getter("createdDate", "java.time.Instant"))
            append(JavaMethodBuilder.setter("lastModifiedDate", "java.time.Instant"))
            append(JavaMethodBuilder.getter("lastModifiedDate", "java.time.Instant"))
            append(JavaMethodBuilder.setter("createdBy", "String"))
            append(JavaMethodBuilder.getter("createdBy", "String"))
            append(JavaMethodBuilder.setter("lastModifiedBy", "String"))
            append(JavaMethodBuilder.getter("lastModifiedBy", "String"))
            append(JavaMethodBuilder.setter("recordStatus", "Integer"))
            append(JavaMethodBuilder.getter("recordStatus", "Integer"))
        }
    }

    override fun getVariableName(): String = getClassName().uncapitalize()

    override fun getClassName(): String = "Auditable"

    override fun getPackage(): String =
        ctx.getPackageName("entity")

    override fun getFQCN(): String =
        getPackage() + "." + getClassName()
}

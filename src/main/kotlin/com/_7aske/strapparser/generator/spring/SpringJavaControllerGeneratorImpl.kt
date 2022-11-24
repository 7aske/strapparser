package com._7aske.strapparser.generator.spring

import com._7aske.strapparser.extensions.toKebabCase
import com._7aske.strapparser.extensions.uncapitalize
import com._7aske.strapparser.generator.ControllerGenerator
import com._7aske.strapparser.generator.DataTypeResolver
import com._7aske.strapparser.generator.EntityGenerator
import com._7aske.strapparser.generator.GeneratorContext
import com.google.googlejavaformat.java.Formatter
import java.nio.file.Path
import java.nio.file.Paths

class SpringJavaControllerGeneratorImpl(
    entity: EntityGenerator,
    ctx: GeneratorContext,
    dataTypeResolver: DataTypeResolver
) : ControllerGenerator(entity, ctx, dataTypeResolver) {
    private val formatter = Formatter()

    override fun getOutputFilePath(): Path =
        Paths.get(
            ctx.getOutputLocation(),
            "src",
            "main",
            "java",
            ctx.getPackageName().replace(".", separator),
            "controller",
            this.resolveClassName() + ".java"
        )

    override fun generate(): String {
        return formatter.formatSource(
            buildString {
                append("package ${ctx.getPackageName("controller")};")
                append("@org.springframework.web.bind.annotation.RestController\n")
                append("@org.springframework.web.bind.annotation.RequestMapping(\"/api/v1/${resolveEndpoint()}\")\n")
                append("public class ")
                    .append(resolveClassName())
                append("{")
                append("}")
            }
        )
    }

    override fun resolveEndpoint(): String =
        this.entity.resolveClassName().toKebabCase().uncapitalize()

    override fun resolveClassName(): String =
        this.entity.resolveClassName() + "Controller"

    override fun generateEndpoints(): String {
        TODO("Not yet implemented")
    }
}

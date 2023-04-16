package com._7aske.strapparser.generator.java

import com._7aske.strapparser.extensions.uncapitalize
import com._7aske.strapparser.generator.BaseGenerator
import com._7aske.strapparser.generator.DataTypeResolver
import com._7aske.strapparser.generator.GeneratorContext

abstract class JavaClassGenerator(
    ctx: GeneratorContext,
    dataTypeResolver: DataTypeResolver,
) : BaseGenerator(ctx, dataTypeResolver) {

    protected val imports = mutableSetOf<String>()

    init {
        // Standard commonly used imports
        import("java.util.*")
        import("java.time.*")
    }

    fun import(imp: String) = imports.add(imp)

    fun getImports() =
        imports.joinToString("\n") {
            "import $it;"
        }

    open fun getVariableName(): String =
        getClassName().uncapitalize()

    abstract fun getClassName(): String

    abstract fun getPackage(): String

    open fun getFQCN(): String = getPackage() + "." + getClassName()
}

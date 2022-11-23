package com._7aske.strapparser.generator.spring

import com._7aske.strapparser.cli.Args
import com._7aske.strapparser.generator.Generator
import com._7aske.strapparser.generator.GeneratorContext
import com._7aske.strapparser.parser.StrapFileResolver
import java.nio.file.Paths

class SpringJavaGeneratorImpl : Generator {

    override fun generate(args: Args) {
        val entities = StrapFileResolver().resolve(Paths.get(args.inputFile))
            .associateBy { it.name }

        val ctx = GeneratorContext(entities, args)

        entities.values.forEach {
            val generator = SpringJavaEntityGeneratorImpl(it, ctx)
            val outPath = generator.getOutputFilePath()

            writeString(outPath, generator.generateEntity())
        }
    }
}

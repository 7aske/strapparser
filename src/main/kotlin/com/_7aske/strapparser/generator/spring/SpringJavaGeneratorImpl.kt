package com._7aske.strapparser.generator.spring

import com._7aske.strapparser.cli.Args
import com._7aske.strapparser.generator.Generator
import com._7aske.strapparser.generator.GeneratorContext
import com._7aske.strapparser.parser.StrapFileResolver
import java.nio.file.Paths

class SpringJavaGeneratorImpl : Generator {
    private val dataTypeResolver = SpringJavaDataTypeResolverImpl()

    override fun generate(args: Args) {
        val entities = StrapFileResolver().resolve(Paths.get(args.inputFile))
            .associateBy { it.name }

        val ctx = GeneratorContext(entities, args)

        entities.values.forEach {
            val entityGenerator = SpringJavaEntityGeneratorImpl(it, ctx, dataTypeResolver)
            val entityOutPath = entityGenerator.getOutputFilePath()

            writeString(entityOutPath, entityGenerator.generate())

            val repositoryGenerator = SpringJavaRepositoryGeneratorImpl(
                entityGenerator,
                ctx,
                dataTypeResolver
            )
            val repositoryOutPath = repositoryGenerator.getOutputFilePath()

            writeString(repositoryOutPath, repositoryGenerator.generate())

            val controllerGenerator = SpringJavaControllerGeneratorImpl(
                entityGenerator,
                ctx,
                dataTypeResolver
            )
            val controllerOutPath = controllerGenerator.getOutputFilePath()

            writeString(controllerOutPath, controllerGenerator.generate())
        }
    }
}

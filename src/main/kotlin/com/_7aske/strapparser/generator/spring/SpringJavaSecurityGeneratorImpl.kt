package com._7aske.strapparser.generator.spring

import com._7aske.strapparser.generator.CompositeGenerator
import com._7aske.strapparser.generator.DataTypeResolver
import com._7aske.strapparser.generator.GeneratorContext
import com._7aske.strapparser.util.writeString

class SpringJavaSecurityGeneratorImpl(
    ctx: GeneratorContext,
    dataTypeResolver: DataTypeResolver
) : CompositeGenerator(ctx, dataTypeResolver) {

    override fun generate() {
        val securityConfigGenerator =
            SpringJavaSecurityConfigGeneratorImpl(ctx, dataTypeResolver)

        writeString(
            securityConfigGenerator.getOutputFilePath(),
            securityConfigGenerator.generate()
        )

        val authenticationRequestGenerator =
            SpringJavaJwtAuthenticationRequestGeneratorImpl(
                ctx,
                dataTypeResolver
            )
        writeString(
            authenticationRequestGenerator.getOutputFilePath(),
            authenticationRequestGenerator.generate()
        )

        val authenticationResponseGenerator =
            SpringJavaJwtAuthenticationResponseGeneratorImpl(
                ctx,
                dataTypeResolver
            )
        writeString(
            authenticationResponseGenerator.getOutputFilePath(),
            authenticationResponseGenerator.generate()
        )

        val springJavaJwtProviderGeneratorImpl =
            SpringJavaJwtProviderGeneratorImpl(ctx, dataTypeResolver)
        writeString(
            springJavaJwtProviderGeneratorImpl.getOutputFilePath(),
            springJavaJwtProviderGeneratorImpl.generate()
        )
    }
}

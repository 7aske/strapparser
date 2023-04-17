package com._7aske.strapparser.generator.spring

import com._7aske.strapparser.generator.CompositeGenerator
import com._7aske.strapparser.generator.DataTypeResolver
import com._7aske.strapparser.generator.GeneratorContext
import com._7aske.strapparser.util.writeString

class SpringKotlinSecurityGeneratorImpl(
    ctx: GeneratorContext,
    dataTypeResolver: DataTypeResolver
) : CompositeGenerator(ctx, dataTypeResolver) {

    override fun generate() {
        val securityConfigGenerator =
            SpringKotlinSecurityConfigGeneratorImpl(ctx, dataTypeResolver)

        writeString(
            securityConfigGenerator.getOutputFilePath(),
            securityConfigGenerator.generate()
        )

        val authenticationRequestGenerator =
            SpringKotlinJwtAuthenticationRequestGeneratorImpl(
                ctx,
                dataTypeResolver
            )
        writeString(
            authenticationRequestGenerator.getOutputFilePath(),
            authenticationRequestGenerator.generate()
        )

        val authenticationResponseGenerator =
            SpringKotlinJwtAuthenticationResponseGeneratorImpl(
                ctx,
                dataTypeResolver
            )
        writeString(
            authenticationResponseGenerator.getOutputFilePath(),
            authenticationResponseGenerator.generate()
        )

        val springKotlinJwtProviderGeneratorImpl =
            SpringKotlinJwtProviderGeneratorImpl(ctx, dataTypeResolver)
        writeString(
            springKotlinJwtProviderGeneratorImpl.getOutputFilePath(),
            springKotlinJwtProviderGeneratorImpl.generate()
        )
    }
}

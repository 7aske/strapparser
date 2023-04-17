package com._7aske.strapparser.generator

import com._7aske.strapparser.cli.Args
import com._7aske.strapparser.generator.spring.SpringJavaGeneratorImpl
import com._7aske.strapparser.generator.spring.SpringKotlinGeneratorImpl

interface Generator {
    fun generate()

    companion object {
        fun create(args: Args): Generator =
            when (args.language) {
                "kotlin" -> SpringKotlinGeneratorImpl(args)
                "java" -> SpringJavaGeneratorImpl(args)
                else -> throw IllegalArgumentException("Language ${args.language} is not supported.")
            }
    }
}

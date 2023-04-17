package com._7aske.strapparser.generator

import com._7aske.strapparser.cli.Args
import com._7aske.strapparser.generator.spring.SpringKotlinGeneratorImpl

interface Generator {
    fun generate(args: Args)

    companion object {
        fun create(): Generator {
            return SpringKotlinGeneratorImpl()
        }
    }
}

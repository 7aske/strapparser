package com._7aske.strapparser.generator

import com._7aske.strapparser.cli.Args
import com._7aske.strapparser.generator.spring.SpringJavaGeneratorImpl
import java.nio.file.Files
import java.nio.file.Path

interface Generator {
    fun generate(args: Args)

    fun writeString(path: Path, string: String) {
        Files.createDirectories(path.parent)
        Files.writeString(path, string)
    }

    companion object {
        fun create(): Generator {
            return SpringJavaGeneratorImpl()
        }
    }
}

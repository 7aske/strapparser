package com._7aske.strapparser.generator.spring

import com._7aske.strapparser.cli.Args
import com._7aske.strapparser.generator.Generator
import com.xenomachina.argparser.ArgParser
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

internal class SpringJavaEntityGeneratorImplTest {

    @Test
    fun generate(@TempDir testDir: Path) {
        val text = """
            entity Post
                field `id` int serial id
                field content string
                field user references User
               
            entity User
                field `id` int serial id
                field username string
        """.trimIndent()

        Files.writeString(testDir.resolve("test.strap"), text)

        val args = ArgParser(
            arrayOf(
                "-o",
                testDir.toString(),
                "-d",
                "com._7aske.backend",
                testDir.resolve("test.strap").toString()
            )
        ).parseInto(::Args)

        val generator = Generator.create()

        generator.generate(args)

        println(Files.readString(testDir
            .resolve("src/main/java/com/_7aske/backend/entity/User.java")
        ))

        println(Files.readString(testDir
            .resolve("src/main/java/com/_7aske/backend/repository/UserRepository.java")
        ))

        println(Files.readString(testDir
            .resolve("src/main/java/com/_7aske/backend/controller/UserController.java")
        ))
    }
}

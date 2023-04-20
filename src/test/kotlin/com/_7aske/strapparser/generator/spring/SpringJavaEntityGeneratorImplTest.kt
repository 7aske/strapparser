package com._7aske.strapparser.generator.spring

import com._7aske.strapparser.cli.Args
import com._7aske.strapparser.generator.Generator
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

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
                field `username` string
        """.trimIndent()

        Files.writeString(testDir.resolve("test.strap"), text)

        val args = Args()
        args.parse(
            arrayOf(
                "-o",
                testDir.toString(),
                "-d",
                "com._7aske",
                "-n",
                "backend",
                "--language",
                "java",
                "-A",
                testDir.resolve("test.strap").toString()
            )
        )

        val generator = Generator.create(args)

        generator.generate()

        println(
            Files.readString(
                testDir
                    .resolve("src/main/java/com/_7aske/backend/entity/User.java")
            )
        )

        println(
            Files.readString(
                testDir
                    .resolve("src/main/java/com/_7aske/backend/repository/UserRepository.java")
            )
        )

        println(
            Files.readString(
                testDir
                    .resolve("src/main/java/com/_7aske/backend/service/UserService.java")
            )
        )

        println(
            Files.readString(
                testDir
                    .resolve("src/main/java/com/_7aske/backend/service/impl/UserServiceImpl.java")
            )
        )

        println(
            Files.readString(
                testDir
                    .resolve("src/main/java/com/_7aske/backend/controller/UserController.java")
            )
        )
    }
}

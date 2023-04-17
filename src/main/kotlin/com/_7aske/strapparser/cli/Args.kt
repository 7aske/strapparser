package com._7aske.strapparser.cli

import com._7aske.strapparser.generator.AvailableDatabases
import com._7aske.strapparser.generator.Generator
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.path

class Args : CliktCommand(printHelpOnEmptyArgs = true) {
    val lombok: Boolean by option(
        "-l", "--lombok",
        help = "use lombok (only for java)"
    ).flag(default = false)

    val auditable: Boolean by option(
        "-a", "--auditable",
        help = "add auditing superclass"
    ).flag(default = false)

    val security: Boolean by option(
        "-s", "--security",
        help = "generate security implementation"
    ).flag(default = false)

    val doc: Boolean by option(
        "--doc",
        help = "generate openapi documentation"
    ).flag(default = false)

    val all: Boolean by option(
        "-A", "--all",
        help = "generate all (entity, repository, service, controller)"
    ).flag(default = false)

    val entity: Boolean by option(
        "-E", "--entity",
        help = "generate entities"
    ).flag(default = false)

    val repository: Boolean by option(
        "-R", "--repository",
        help = "generate repositories"
    ).flag(default = false)

    val service: Boolean by option(
        "-S", "--service",
        help = "generate services"
    ).flag(default = false)

    val controller: Boolean by option(
        "-C", "--controller",
        help = "generate controllers"
    ).flag(default = false)

    val specification: Boolean by option(
        "-p", "--specification",
        help = "repositories will extend JPA Specification"
    ).flag(default = false)

    val output: String by option(
        "-o", "--output",
        help = "output directory",
    ).default("./")

    val domain: String by option(
        "-d", "--domain",
        help = "domain/package of the project (e.g. com.example)"
    ).default("com.example")

    val name by option(
        "-n", "--name",
        help = "name of project (e.g. app)"
    ).default("app")

    val javaVersion by option(
        "--javaVersion",
        help = "java version"
    ).default("17")

    val kotlinVersion by option(
        "--kotlinVersion",
        help = "kotlin version"
    ).default("1.8.20")

    val springVersion by option(
        "--springVersion",
        help = "spring boot version"
    ).default("3.0.5")

    val packaging by option(
        "--packaging",
        help = "packaging type (e.g. jar, war)"
    ).default("jar")

    val database: AvailableDatabases by option(
        "--database",
        help = "database type (e.g. mysql, postgres, mariadb, mongodb)"
    ).enum<AvailableDatabases>()
        .default(AvailableDatabases.POSTGRES)

    val databaseUser: String? by option(
        "--databaseUser",
        help = "database user"
    )

    val databasePass: String? by option(
        "--databasePass",
        help = "database password"
    )

    val databaseHost: String? by option(
        "--databaseHost",
        help = "database host"
    )

    val databasePort: Int? by option(
        "--databasePort",
        help = "database port"
    ).int()

    val databaseName: String? by option(
        "--databaseName",
        help = "database name"
    )

    val language: String by option(
        "--language",
        help = "language (e.g. java, kotlin)"
    ).default("kotlin")

    val inputFile by argument(
        help = "input .strap file"
    ).path(
        mustExist = true,
        canBeDir = true,
        canBeFile = true,
        mustBeReadable = true
    )

    override fun run() {
        Generator.create(this).generate()
    }
}

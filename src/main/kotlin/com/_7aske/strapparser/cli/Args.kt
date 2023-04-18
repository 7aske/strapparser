package com._7aske.strapparser.cli

import com._7aske.strapparser.generator.AvailableDatabases
import com._7aske.strapparser.generator.Generator
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.path

class Args : CliktCommand(printHelpOnEmptyArgs = true) {
    val lombok: Boolean by option(
        "--lombok",
        help = "Use Lombok (useful only for Java)"
    ).flag(default = false)

    val auditable: Boolean by option(
        "--auditable",
        help = "Add database auditing"
    ).flag(default = false)

    val security: Boolean by option(
        "--security",
        help = "Generate basic JWT security implementation"
    ).flag(default = false)

    val doc: Boolean by option(
        "--doc",
        help = "Generate openapi documentation and configuration"
    ).flag(default = false)

    val all: Boolean by option(
        "-A", "--all",
        help = "Generate everything (entity, repository, service, controller)"
    ).flag(default = false)

    val entity: Boolean by option(
        "-E", "--entity",
        help = "Generate entities"
    ).flag(default = false)

    val repository: Boolean by option(
        "-R", "--repository",
        help = "Generate repositories"
    ).flag(default = false)

    val service: Boolean by option(
        "-S", "--service",
        help = "Generate services"
    ).flag(default = false)

    val controller: Boolean by option(
        "-C", "--controller",
        help = "Generate controllers"
    ).flag(default = false)

    val specification: Boolean by option(
        "--specification",
        help = "Repositories will extend JPA Specification"
    ).flag(default = false)

    val output: String by option(
        "-o", "--output",
        help = "Output directory (project root)",
    ).path(
        mustExist = false,
        canBeDir = true,
        canBeFile = false,
    ).convert { it.toString() }.default("./")

    val domain: String by option(
        "-d", "--domain",
        help = "Domain/package of the project (e.g. com.example)"
    ).default("com.example")

    val name by option(
        "-n", "--name",
        help = "Name of project (e.g. app)"
    ).default("app")

    val javaVersion by option(
        "--javaVersion",
        help = "Java version"
    ).default("17")

    val kotlinVersion by option(
        "--kotlinVersion",
        help = "Kotlin version"
    ).default("1.8.20")

    val springVersion by option(
        "--springVersion",
        help = "Spring Boot version"
    ).default("3.0.5")

    val packaging by option(
        "--packaging",
        help = "Packaging type (e.g. jar, war)"
    ).default("jar")

    val database: AvailableDatabases by option(
        "--database",
        help = "Database type (e.g. mysql, postgres, mariadb, mongodb)"
    ).enum<AvailableDatabases>()
        .default(AvailableDatabases.POSTGRES)

    val databaseUser: String? by option(
        "--databaseUser",
        help = "Database user"
    )

    val databasePass: String? by option(
        "--databasePass",
        help = "Database password"
    )

    val databaseHost: String? by option(
        "--databaseHost",
        help = "Database host"
    )

    val databasePort: Int? by option(
        "--databasePort",
        help = "Database port"
    ).int()

    val databaseName: String? by option(
        "--databaseName",
        help = "Database name"
    )

    val language: String by option(
        "--language",
        help = "Language (e.g. java, kotlin)"
    ).default("kotlin")

    val inputFile by argument(
        help = "Input .strap file"
    ).path(
        mustExist = true,
        canBeDir = false,
        canBeFile = true,
        mustBeReadable = true
    )

    override fun run() {
        Generator.create(this).generate()
    }
}

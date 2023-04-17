package com._7aske.strapparser.cli

import com._7aske.strapparser.generator.AvailableDatabases
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default

class Args(parser: ArgParser) {
    val lombok by parser.flagging(
        "-l", "--lombok",
        help = "use lombok (only for java)"
    )

    val auditable by parser.flagging(
        "-a", "--auditable",
        help = "add auditing superclass"
    )

    val security by parser.flagging(
        "-s", "--security",
        help = "generate security implementation"
    )

    val doc by parser.flagging(
        "--doc",
        help = "generate openapi documentation"
    )

    val all by parser.flagging(
        "-A", "--all",
        help = "generate all (entity, repository, service, controller)"
    )

    val entity by parser.flagging(
        "-E", "--entity",
        help = "generate entities"
    )

    val repository by parser.flagging(
        "-R", "--repository",
        help = "generate repositories"
    )

    val service by parser.flagging(
        "-S", "--service",
        help = "generate services"
    )

    val controller by parser.flagging(
        "-C", "--controller",
        help = "generate controllers"
    )

    val specification by parser.flagging(
        "-p", "--specification",
        help = "repositories will extend JPA Specification"
    )

    val output by parser.storing(
        "-o", "--output",
        help = "output directory",
    ).default("./")

    val domain by parser.storing(
        "-d", "--domain",
        help = "domain/package of the project (e.g. com.example)"
    ).default("com.example")

    val name by parser.storing(
        "-n", "--name",
        help = "name of project (e.g. app)"
    ).default("app")

    val javaVersion by parser.storing(
        "--javaVersion",
        help = "java version"
    ).default("17")

    val kotlinVersion by parser.storing(
        "--kotlinVersion",
        help = "kotlin version"
    ).default("1.8.20")

    val springVersion by parser.storing(
        "--springVersion",
        help = "spring boot version"
    ).default("3.0.5")

    val packaging by parser.storing(
        "--packaging",
        help = "packaging type (e.g. jar, war)"
    ).default("jar")

    val database: AvailableDatabases by parser.storing(
        "--database",
        help = "database type (e.g. mysql, postgres, mariadb, mongodb)"
    ) {
        AvailableDatabases.valueOf(this.uppercase())
    }.default(AvailableDatabases.POSTGRES)

    val databaseUser: String? by parser.storing(
        "--databaseUser",
        help = "database user"
    ).default(null)

    val databasePass: String? by parser.storing(
        "--databasePass",
        help = "database password"
    ).default(null)

    val databaseHost: String? by parser.storing(
        "--databaseHost",
        help = "database host"
    ).default(null)

    val databasePort: Int? by parser.storing(
        "--databasePort",
        help = "database port"
    ) {
        this.toInt()
    }.default(null)

    val databaseName: String? by parser.storing(
        "--databaseName",
        help = "database name"
    ).default(null)

    val inputFile by parser.positional(
        "FILE",
        help = "input .strap file"
    )
}

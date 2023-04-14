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
        help = "domain/package of the project (e.g. com.example.app)"
    )

    val name by parser.storing(
        "-n", "--name",
        help = "name of project (e.g. app)"
    )

    val javaVersion by parser.storing(
        "--javaVersion",
        help = "java version"
    ).default("17")

    val springVersion by parser.storing(
        "--springVersion",
        help = "spring boot version"
    ).default("3.0.5")

    val packaging by parser.storing(
        "--packaging",
        help = "packaging type (e.g. jar, war)"
    ).default("jar")

    val database: AvailableDatabases by parser.mapping(
        "--mongodb" to AvailableDatabases.MONGODB,
        "--mariadb" to AvailableDatabases.MARIADB,
        "--mysql" to AvailableDatabases.MYSQL,
        "--postgres" to AvailableDatabases.POSTGRES,
        help = "database type (e.g. mysql, postgres, mariadb, mongodb)"
    ).default(AvailableDatabases.POSTGRES)

    val inputFile by parser.positional(
        "FILE",
        help = "input .strap file"
    )
}

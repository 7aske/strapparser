package com._7aske.strapparser.cli

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

    val verbose by parser.flagging(
        "-v", "--verbose",
        help = "enable verbose mode"
    )

    val overwrite by parser.flagging(
        "-w", "--overwrite",
        help = "overwrite existing files"
    )

    val output by parser.storing(
        "-o", "--output",
        help = "output directory",
    ).default("./")

    val domain by parser.storing(
        "-d", "--domain",
        help = "domain/package of the project (e.g. com.example.app)"
    ).default("")

    val inputFile by parser.positional(
        "FILE",
        help = "input .strap file"
    )
}

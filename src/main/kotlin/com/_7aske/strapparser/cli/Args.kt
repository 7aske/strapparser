package com._7aske.strapparser.cli

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default

class Args(parser: ArgParser) {
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

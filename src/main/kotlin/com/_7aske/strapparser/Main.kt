package com._7aske.strapparser

import com._7aske.strapparser.cli.Args
import com._7aske.strapparser.generator.Generator
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody

fun main(args: Array<String>) = mainBody {
    ArgParser(args).parseInto(::Args).run {
        Generator.create().generate(this)
    }
}

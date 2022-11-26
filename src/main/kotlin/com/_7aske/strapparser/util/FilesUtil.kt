package com._7aske.strapparser.util

import java.nio.file.Files
import java.nio.file.Path

fun writeString(path: Path, string: String) {
    Files.createDirectories(path.parent)
    Files.writeString(
        path,
        string
    )
}

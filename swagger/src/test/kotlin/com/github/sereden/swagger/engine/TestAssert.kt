package com.github.sereden.swagger.engine

import java.io.File
import kotlin.test.assertEquals

fun assertEquals(fileManager: TestFileManager, expectedFilePath: String, actualFileContent: String) {
    assertEquals(
        fileManager.readFile(File(expectedFilePath)),
        actualFileContent.normalize()
    )
}

private fun String.normalize(): String {
    val lines = lines()
        .dropWhile { it.isBlank() }
        .dropLastWhile { it.isBlank() }

    val spaceLines = lines.map { it.replace("\t", "    ") } // Replace tabs with 4 spaces
    val minIndent = spaceLines
        .filter { it.isNotBlank() }
        .map { it.indexOfFirst { ch -> !ch.isWhitespace() }.takeIf { it >= 0 } ?: it.length }
        .minOrNull() ?: 0

    return spaceLines.joinToString("\n") { line ->
        if (line.isBlank()) "" else line.drop(minIndent)
    }
}

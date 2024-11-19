package com.github.sereden.swagger.primitive

import com.github.sereden.swagger.engine.TestExtension
import com.github.sereden.swagger.engine.TestFileAppendable
import com.github.sereden.swagger.engine.TestFileManager
import com.github.sereden.swagger.engine.assertEquals
import com.github.sereden.swagger.processor.CodeGeneratorProcessor
import kotlin.test.Test
import java.io.File

class ParseEnumTest {

    @Test
    fun `process should generate enum with name Options and property when type is string enum`() {
        val appendable = TestFileAppendable()
        val fileManager = TestFileManager(appendable)
        CodeGeneratorProcessor(
            extension = TestExtension(
                testFilePath = File("input/primitive/string-enum.json")
            )
        ).process(fileManager)
        assertEquals(
            fileManager = fileManager,
            expectedFilePath = "output/primitive/StringEnumResponse.kt",
            actualFileContent = appendable.text
        )
    }
}
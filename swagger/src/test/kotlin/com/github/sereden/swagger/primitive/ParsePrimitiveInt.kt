package com.github.sereden.swagger.primitive

import com.github.sereden.swagger.engine.TestExtension
import com.github.sereden.swagger.engine.TestFileAppendable
import com.github.sereden.swagger.engine.TestFileManager
import com.github.sereden.swagger.engine.assertEquals
import com.github.sereden.swagger.processor.CodeGeneratorProcessor
import kotlin.test.Test
import java.io.File

class ParsePrimitiveInt {

    @Test
    fun `process should generate single Int property when type is integer`() {
        val appendable = TestFileAppendable()
        val fileManager = TestFileManager(appendable)
        CodeGeneratorProcessor(
            extension = TestExtension(
                testFilePath = File("input/primitive/integer-single.json")
            )
        ).process(fileManager)
        assertEquals(
            fileManager = fileManager,
            expectedFilePath = "output/primitive/IntegerSingleResponse.kt",
            actualFileContent = appendable.text
        )
    }

    @Test
    fun `process should generate List of Int properties when type is integer array`() {
        val appendable = TestFileAppendable()
        val fileManager = TestFileManager(appendable)
        CodeGeneratorProcessor(
            extension = TestExtension(
                testFilePath = File("input/primitive/integer-array.json")
            )
        ).process(fileManager)
        assertEquals(
            fileManager = fileManager,
            expectedFilePath = "output/primitive/IntegerListResponse.kt",
            actualFileContent = appendable.text
        )
    }
}
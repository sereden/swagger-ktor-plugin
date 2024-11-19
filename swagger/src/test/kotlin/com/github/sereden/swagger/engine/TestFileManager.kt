package com.github.sereden.swagger.engine

import com.github.sereden.swagger.processor.file.FileManager
import com.squareup.kotlinpoet.FileSpec
import java.io.File

class TestFileManager(private val appendable: Appendable) : FileManager {
    override fun write(fileSpec: FileSpec) {
        fileSpec.writeTo(appendable)
    }

    override fun readFile(file: File): String {
        val resource = this::class.java.classLoader.getResource(file.path)
            ?: throw IllegalArgumentException("File not found!")
        return resource.readText()
    }
}
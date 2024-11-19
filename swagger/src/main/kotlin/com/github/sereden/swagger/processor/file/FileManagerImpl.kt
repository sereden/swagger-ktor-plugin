package com.github.sereden.swagger.processor.file

import com.squareup.kotlinpoet.FileSpec
import java.io.File

class FileManagerImpl(private val directory: File) : FileManager {
    init {
        directory.mkdirs()
    }

    override fun write(fileSpec: FileSpec) {
        fileSpec.writeTo(directory)
    }

    override fun readFile(file: File): String {
        return file.readText()
    }
}
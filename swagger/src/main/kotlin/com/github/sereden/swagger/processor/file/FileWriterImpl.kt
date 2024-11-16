package com.github.sereden.swagger.processor.file

import com.squareup.kotlinpoet.FileSpec
import java.io.File

class FileWriterImpl(private val directory: File) : FileWriter {
    override fun write(fileSpec: FileSpec) {
        fileSpec.writeTo(directory)
    }
}
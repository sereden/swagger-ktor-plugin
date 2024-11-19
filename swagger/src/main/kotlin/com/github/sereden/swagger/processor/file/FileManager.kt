package com.github.sereden.swagger.processor.file

import com.squareup.kotlinpoet.FileSpec
import java.io.File

interface FileManager {
    fun write(fileSpec: FileSpec)
    fun readFile(file: File): String
}
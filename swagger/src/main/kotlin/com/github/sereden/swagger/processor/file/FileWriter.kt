package com.github.sereden.swagger.processor.file

import com.squareup.kotlinpoet.FileSpec

interface FileWriter {
    fun write(fileSpec: FileSpec)
}
package com.github.sereden.swagger.engine

import com.github.sereden.swagger.plugin.SwaggerExtensions
import java.io.File

data class TestExtension(val testFilePath: File?) : SwaggerExtensions() {

    init {
        path = testFilePath
        packageName = "com.test"
        serverUrl = "https://remote.com"
    }
}

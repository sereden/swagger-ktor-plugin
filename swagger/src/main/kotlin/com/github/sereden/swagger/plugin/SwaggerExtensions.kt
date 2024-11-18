package com.github.sereden.swagger.plugin

import java.io.File

open class SwaggerExtensions {
    var path: File? = null
    var modelPropertyPath: String = "definitions"
    var packageName: String? = null
    var exclude: File? = null
    var serverUrl: String? = null
}

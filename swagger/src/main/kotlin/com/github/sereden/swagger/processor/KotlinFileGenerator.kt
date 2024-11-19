package com.github.sereden.swagger.processor

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec

class KotlinFileGenerator(private val packageName: String) {
    // Generate Kotlin file with given name in given package
    operator fun invoke(
        className: String,
        typeSpecs: () -> List<TypeSpec>
    ): FileSpec.Builder {
        return FileSpec.builder(packageName, className)
            .indent("\t")
            .apply {
                typeSpecs().forEach { typeSpec ->
                    addType(typeSpec)
                }
            }
    }
}
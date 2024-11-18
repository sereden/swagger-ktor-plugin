package com.github.sereden.swagger.utils

import org.gradle.internal.extensions.stdlib.capitalized

fun String.getReferenceClassName(): String {
    return substringAfterLast("/")
}

fun String.toCamelCase(): String {
    return lowercase()
        .capitalized()
        .split('_')
        .joinToString("", transform = String::capitalized)
}
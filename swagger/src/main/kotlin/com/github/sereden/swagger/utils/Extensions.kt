package com.github.sereden.swagger.utils

fun String.getReferenceClassName(): String {
    return substringAfterLast("/")
}
package com.github.sereden.swagger.processor

import org.json.JSONObject
import java.io.File

// Handles whether the current entity should be handled by the engined or skipped
class ProcessingEntityManager(file: File?) {
    private val jsonObject: JSONObject? = file?.let { file -> JSONObject(file.readText()) }
    private val properties = jsonObject?.optJSONArray(KEY_PROPERTY)
    private val paths = jsonObject?.optJSONArray(KEY_PATH)

    fun canProceed(name: String, type: ExcludeEntityType): Boolean {
        val array = when (type) {
            ExcludeEntityType.Property -> properties
            ExcludeEntityType.Path -> paths
        }
        return array?.find { item -> item as String? == name } == null
    }

    enum class ExcludeEntityType {
        Property,
        Path
    }

    companion object {
        private const val KEY_PATH = "path"
        private const val KEY_PROPERTY = "property"
    }
}

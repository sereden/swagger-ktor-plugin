package com.github.sereden.swagger.processor

import com.squareup.kotlinpoet.TypeSpec
import org.json.JSONArray
import org.json.JSONObject

class ProcessModelItem(
    private val models: JSONObject,
    private val processProperties: ProcessProperties,
) {
    operator fun invoke(
        modelItem: JSONObject,
        className: String,
        classBuilder: TypeSpec.Builder,
        optional: Boolean = false
    ) {
        // Get properties for the model and generate each property
        val properties = getProperties(modelItem)
        val allArray = modelItem.optJSONArray("allOf") ?: JSONArray()
        val oneOfArray = modelItem.optJSONArray("oneOf") ?: JSONArray()
        // TODO handle required
        if (!allArray.isEmpty) {
            allArray.forEach { allArrayItem ->
                val allJsonItem = allArrayItem as JSONObject
                when {
                    getReferenceClass(allJsonItem) != null -> {
                        val reference = getReferenceClass(allJsonItem)
                        invoke(
                            modelItem = models.optJSONObject(reference),
                            className = className,
                            classBuilder = classBuilder,
                        )
                    }
                    else -> {
                        val nestedProperties = getProperties(allArrayItem)
                        processProperties(
                            className = className,
                            properties = nestedProperties,
                            classBuilder = classBuilder,
                            optional = optional
                        )
                    }
                }
            }
        }
        if (!oneOfArray.isEmpty) {
            oneOfArray.filter { oneOfArrayItem ->
                getReferenceClass(oneOfArrayItem as JSONObject) != className
            }.forEach { oneOfArrayItem ->
                val reference = getReferenceClass(oneOfArrayItem as JSONObject)
                invoke(
                    modelItem = models.optJSONObject(reference),
                    className = className,
                    classBuilder = classBuilder,
                )
            }
        }

        if (!properties.isEmpty) {
            processProperties(
                className = className,
                properties = properties,
                classBuilder = classBuilder,
                optional = optional
            )
        }
    }

    private fun getProperties(schemaObject: JSONObject?) = schemaObject?.optJSONObject("properties") ?: JSONObject()

    private fun getReferenceClass(jsonObject: JSONObject): String? {
        val propertyRef = jsonObject.optString("\$ref")
        return if (propertyRef.isNotEmpty()) {
            propertyRef.substringAfterLast("/").ifBlank { null }
        } else {
            null
        }
    }
}
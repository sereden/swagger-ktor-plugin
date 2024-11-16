package com.github.sereden.swagger.processor

import com.squareup.kotlinpoet.TypeSpec
import org.json.JSONArray
import org.json.JSONObject

class ProcessSchemaItem(
    private val schemas: JSONObject,
    private val processProperties: ProcessProperties,
) {
    operator fun invoke(
        schemaItem: JSONObject,
        className: String,
        classBuilder: TypeSpec.Builder,
        optional: Boolean = false
    ) {
        // Get properties for the schema and generate each property
        val properties = getProperties(schemaItem)
        val allArray = schemaItem.optJSONArray("allOf") ?: JSONArray()
        val oneOfArray = schemaItem.optJSONArray("oneOf") ?: JSONArray()
        // TODO handle required
        if (!allArray.isEmpty) {
            allArray.forEach { allArrayItem ->
                val allJsonItem = allArrayItem as JSONObject
                when {
                    getReferenceClass(allJsonItem) != null -> {
                        val reference = getReferenceClass(allJsonItem)
                        invoke(
                            schemaItem = schemas.optJSONObject(reference),
                            className = className,
                            classBuilder = classBuilder,
                        )
                    }
                    else -> {
                        val nestedProperties = getProperties(allArrayItem)
                        processProperties(
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
                    schemaItem = schemas.optJSONObject(reference),
                    className = className,
                    classBuilder = classBuilder,
                )
            }
        }

        if (!properties.isEmpty) {
            processProperties(
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
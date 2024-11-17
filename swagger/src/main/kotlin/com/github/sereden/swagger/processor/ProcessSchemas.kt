package com.github.sereden.swagger.processor

import com.github.sereden.swagger.processor.file.FileWriter
import com.github.sereden.swagger.processor.structure.KotlinDataClassGenerator
import org.json.JSONObject

class ProcessSchemas(
    private val schemas: JSONObject,
    private val fileWriter: FileWriter,
    private val processSchemaItem: ProcessSchemaItem,
    private val dataClassGenerator: KotlinDataClassGenerator,
    private val kotlinFileGenerator: KotlinFileGenerator,
    private val processingEntityManager: ProcessingEntityManager
) {
    fun process() {
        // Generate a Kotlin data class for each schema in the Swagger JSON
        schemas.keys().forEach { schemaItemKey ->
            if (processingEntityManager.canProceed(schemaItemKey, ProcessingEntityManager.ExcludeEntityType.Property)) {
                val schemaItemObject = schemas.getJSONObject(schemaItemKey)
                val typeSpec = dataClassGenerator(className = schemaItemKey) { classBuilder ->
                    processSchemaItem(
                        schemaItem = schemaItemObject,
                        className = schemaItemKey,
                        classBuilder = classBuilder,
                    )
                }.build()
                val fileSpec = kotlinFileGenerator(schemaItemKey) {
                    listOf(typeSpec)
                }
                fileWriter.write(fileSpec.build())
                println("Dto file $schemaItemKey has been generated")
            } else {
                println("$schemaItemKey is added to exclude list. Ignore handling")
            }
        }
    }
}
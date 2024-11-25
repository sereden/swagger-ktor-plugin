package com.github.sereden.swagger.processor

import com.github.sereden.swagger.processor.file.FileManager
import com.github.sereden.swagger.processor.structure.KotlinDataClassGenerator
import org.json.JSONObject

class ProcessModels(
    private val models: JSONObject,
    private val fileManager: FileManager,
    private val processModelItem: ProcessModelItem,
    private val dataClassGenerator: KotlinDataClassGenerator,
    private val kotlinFileGenerator: KotlinFileGenerator,
    private val processingEntityManager: ProcessingEntityManager
) {
    fun process() {
        // Generate a Kotlin data class for each schema in the Swagger JSON
        models.keys().forEach { schemaItemKey ->
            if (processingEntityManager.canProceed(schemaItemKey, ProcessingEntityManager.ExcludeEntityType.Property)) {
                val modelsItemObject = models.getJSONObject(schemaItemKey)
                val typeSpec = dataClassGenerator(className = schemaItemKey) { classBuilder ->
                    processModelItem(
                        modelItem = modelsItemObject,
                        className = schemaItemKey,
                        classBuilder = classBuilder,
                    )
                }.build()
                val fileSpec = kotlinFileGenerator(schemaItemKey) {
                    listOf(typeSpec)
                }
                fileManager.write(fileSpec.build())
                println("Dto file $schemaItemKey has been generated")
            } else {
                println("$schemaItemKey is added to exclude list. Ignore handling")
            }
        }
    }
}
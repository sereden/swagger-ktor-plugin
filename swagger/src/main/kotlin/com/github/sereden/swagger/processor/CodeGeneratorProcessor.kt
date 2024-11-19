package com.github.sereden.swagger.processor

import com.github.sereden.swagger.plugin.SwaggerExtensions
import com.github.sereden.swagger.processor.file.FileManager
import com.github.sereden.swagger.processor.structure.KotlinDataClassGenerator
import com.github.sereden.swagger.processor.structure.KotlinEnumClassGenerator
import com.github.sereden.swagger.processor.structure.KotlinObjectClassGenerator
import com.github.sereden.swagger.processor.structure.KotlinSealedInterfaceGenerator
import com.github.sereden.swagger.processor.usecase.GenerateSealedInterfacePropertyUseCase
import org.json.JSONObject

class CodeGeneratorProcessor(
    private val extension: SwaggerExtensions
) {
    fun process(fileManager: FileManager) {
        // The exported JSON file that defines REST API
        val swaggerFile = extension.path ?: throw IllegalStateException("Swagger JSON is not provided")
        // Package name where files should be placed
        val packageName = extension.packageName ?: throw IllegalStateException("Package name is not provided")
        // If JSON that defines what should be ignored
        val excludeFile = extension.exclude
        val modelPath = extension.modelPropertyPath
        val jsonObject = JSONObject(fileManager.readFile(swaggerFile))
        val schemas = getSchemaJsonObject(jsonObject, modelPath)
        val kotlinDataClassGenerator = KotlinDataClassGenerator()
        val kotlinFileGenerator = KotlinFileGenerator(packageName)
        val appendClassProperty = AppendClassPropertyManager()
        val processingEntityManager = ProcessingEntityManager(excludeFile)
        val generateSealedInterfacePropertyUseCase = GenerateSealedInterfacePropertyUseCase(
            packageName = packageName,
            sealedInterfaceGenerator = KotlinSealedInterfaceGenerator(packageName),
            dataClassGenerator = kotlinDataClassGenerator,
            appendClassProperty = appendClassProperty,
            fileGenerator = kotlinFileGenerator,
            kotlinObjectClassGenerator = KotlinObjectClassGenerator(),
            fileManager = fileManager,
        )
        ProcessSchemas(
            schemas = schemas,
            fileManager = fileManager,
            processSchemaItem = ProcessSchemaItem(
                schemas = schemas,
                processProperties = ProcessProperties(
                    packageName = packageName,
                    fileManager = fileManager,
                    generateSealedInterfacePropertyUseCase = generateSealedInterfacePropertyUseCase,
                    appendClassProperty = appendClassProperty,
                    appendEnumProperty = AppendEnumPropertyManager(),
                    enumClassGenerator = KotlinEnumClassGenerator(),
                    fileGenerator = kotlinFileGenerator
                )
            ),
            dataClassGenerator = kotlinDataClassGenerator,
            kotlinFileGenerator = kotlinFileGenerator,
            processingEntityManager = processingEntityManager
        ).process()
    }

    private fun getSchemaJsonObject(jsonObject: JSONObject, modelPath: String): JSONObject {
        var result = jsonObject
        modelPath.split("/").forEach { path ->
            result = result.getJSONObject(path)
        }
        return result
    }
}

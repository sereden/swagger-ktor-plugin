package com.github.sereden.swagger.processor

import com.github.sereden.swagger.plugin.SwaggerExtensions
import com.github.sereden.swagger.processor.file.FileWriterImpl
import com.github.sereden.swagger.processor.structure.KotlinDataClassGenerator
import com.github.sereden.swagger.processor.structure.KotlinEnumClassGenerator
import com.github.sereden.swagger.processor.structure.KotlinObjectClassGenerator
import com.github.sereden.swagger.processor.structure.KotlinSealedInterfaceGenerator
import com.github.sereden.swagger.processor.usecase.GenerateSealedInterfacePropertyUseCase
import org.gradle.api.Project
import org.json.JSONObject
import java.io.File

class CodeGeneratorProcessor(
    private val target: Project,
    private val extension: SwaggerExtensions
) {
    fun process() {
        // The exported JSON file that defines REST API
        val swaggerFile = extension.path ?: throw IllegalStateException("Swagger JSON is not provided")
        // Package name where files should be placed
        val packageName = extension.packageName ?: throw IllegalStateException("Package name is not provided")
        // If JSON that defines what should be ignored
        val excludeFile = extension.exclude
        val modelPath = extension.modelPropertyPath
        // TODO debug/release
        val outputDir = File("${target.projectDir}/build/generated/swagger/metadata/commonMain/kotlin")
        outputDir.mkdirs()
        val jsonObject = JSONObject(swaggerFile.readText())
        val schemas = getSchemaJsonObject(jsonObject, modelPath)
        val fileWriter = FileWriterImpl(outputDir)
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
            fileWriter = fileWriter,
        )
        ProcessSchemas(
            schemas = schemas,
            fileWriter = fileWriter,
            processSchemaItem = ProcessSchemaItem(
                schemas = schemas,
                processProperties = ProcessProperties(
                    packageName = packageName,
                    fileWriter = fileWriter,
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

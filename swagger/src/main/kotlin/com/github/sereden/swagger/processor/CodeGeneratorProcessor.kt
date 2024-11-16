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
        val swaggerFile = extension.path ?: throw IllegalStateException("Swagger JSON is not provided")
        val packageName = extension.packageName ?: throw IllegalStateException("Package name is not provided")
        // TODO debug/release
        val outputDir = File("${target.projectDir}/build/generated/swagger/metadata/commonMain/kotlin")
        outputDir.mkdirs()
        val jsonObject = JSONObject(swaggerFile.readText())
        val schemas = jsonObject
            .getJSONObject("components")
            .optJSONObject("schemas") ?: JSONObject()
        val fileWriter = FileWriterImpl(outputDir)
        val kotlinDataClassGenerator = KotlinDataClassGenerator()
        val kotlinFileGenerator = KotlinFileGenerator(packageName)
        val appendClassProperty = AppendClassPropertyManager()
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
            kotlinFileGenerator = kotlinFileGenerator
        ).process()
    }
}

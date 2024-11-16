package com.github.sereden.swagger.processor.usecase

import com.github.sereden.swagger.processor.AppendClassPropertyManager
import com.github.sereden.swagger.processor.KotlinFileGenerator
import com.github.sereden.swagger.processor.file.FileWriter
import com.github.sereden.swagger.processor.structure.KotlinDataClassGenerator
import com.github.sereden.swagger.processor.structure.KotlinObjectClassGenerator
import com.github.sereden.swagger.processor.structure.KotlinSealedInterfaceGenerator
import com.github.sereden.swagger.utils.getReferenceClassName
import com.squareup.kotlinpoet.ClassName
import org.json.JSONArray
import org.json.JSONObject

class GenerateSealedInterfacePropertyUseCase(
    private val packageName: String,
    private val sealedInterfaceGenerator: KotlinSealedInterfaceGenerator,
    private val dataClassGenerator: KotlinDataClassGenerator,
    private val appendClassProperty: AppendClassPropertyManager,
    private val fileGenerator: KotlinFileGenerator,
    private val kotlinObjectClassGenerator: KotlinObjectClassGenerator,
    private val fileWriter: FileWriter
) {
    operator fun invoke(
        sealedInterfaceName: String,
        oneOfArray: JSONArray
    ) {
        val interfaceTypeSpec = sealedInterfaceGenerator(sealedInterfaceName)
        val sealedFile = fileGenerator.invoke(sealedInterfaceName) {
            val nestedFileNames = oneOfArray.mapNotNull { oneOf ->
                (oneOf as JSONObject?)?.getString("\$ref")?.getReferenceClassName()
            }
            val nestedFiles = nestedFileNames.map { oneOfClassName ->
                val nestedDto = "${oneOfClassName}Option"
                dataClassGenerator(nestedDto) { classBuilder ->
                    appendClassProperty(
                        classBuilder = classBuilder,
                        propertyName = "value",
                        nullable = false,
                        description = "",
                        example = "",
                        ClassName(packageName, oneOfClassName)
                    )
                }.addSuperinterface(ClassName(packageName, sealedInterfaceName))
                    .build()
            }
            val serializer = kotlinObjectClassGenerator(
                className = sealedInterfaceName,
                options = nestedFileNames
            ).build()

            listOf(interfaceTypeSpec) + nestedFiles + serializer
        }
            .addImport("kotlinx.serialization.json", "jsonObject")
            .addImport("kotlinx.serialization.json", "Json")
            .build()
        fileWriter.write(sealedFile)
    }
}
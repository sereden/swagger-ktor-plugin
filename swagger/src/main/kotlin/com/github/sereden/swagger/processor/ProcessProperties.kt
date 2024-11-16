package com.github.sereden.swagger.processor

import com.github.sereden.swagger.processor.file.FileWriter
import com.github.sereden.swagger.processor.structure.KotlinEnumClassGenerator
import com.github.sereden.swagger.processor.usecase.GenerateSealedInterfacePropertyUseCase
import com.github.sereden.swagger.utils.getReferenceClassName
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import org.gradle.internal.extensions.stdlib.capitalized
import org.json.JSONObject

class ProcessProperties(
    private val packageName: String,
    private val fileWriter: FileWriter,
    private val generateSealedInterfacePropertyUseCase: GenerateSealedInterfacePropertyUseCase,
    private val appendClassProperty: AppendClassPropertyManager,
    private val appendEnumProperty: AppendEnumPropertyManager,
    private val enumClassGenerator: KotlinEnumClassGenerator,
    private val fileGenerator: KotlinFileGenerator
) {

    operator fun invoke(
        properties: JSONObject,
        classBuilder: TypeSpec.Builder,
        optional: Boolean
    ) {
        properties.keys().forEach { propertyName ->
            val propertyObject = properties.getJSONObject(propertyName)
            val propertyType = propertyObject.optString("type")
            val propertyItems = propertyObject.optJSONObject("items")
            val propertyOneOf = propertyObject.has("oneOf")
            val propertyRef = propertyObject.has("\$ref")
            val propertyDescription = propertyObject.optString("description")
            val propertyExample = propertyObject.optString("example")
            when {
                propertyType == "object" -> {
                    propertyItems?.keys()?.forEach { itemKey ->
                        when (itemKey) {
                            "\$ref" -> {
                                handleReferenceProperty(
                                    classBuilder = classBuilder,
                                    propertyName = propertyName,
                                    propertyType = propertyItems.getString(itemKey).getReferenceClassName(),
                                    propertyDescription = propertyDescription,
                                    propertyExample = propertyExample
                                )
                            }
                            else -> throw IllegalStateException("Unknown key: $itemKey")
                        }
                    }
                }
                propertyType == "array" -> {
                    val arrayReference = propertyItems.optString("\$ref")
                    val arrayOneOf = propertyItems.optString("oneOf")
                    val arrayType = propertyItems.optString("type")
                    val resolvedType = when {
                        arrayReference.isNotEmpty() -> {
                            LIST.parameterizedBy(
                                ClassName(
                                    packageName,
                                    arrayReference.getReferenceClassName()
                                )
                            )
                        }
                        arrayOneOf.isNotEmpty() -> {
                            val sealedInterfaceName = "${propertyName.capitalized()}Interface"
                            val oneOfArray = propertyItems.optJSONArray("oneOf")
                            generateSealedInterfacePropertyUseCase(
                                sealedInterfaceName = sealedInterfaceName,
                                oneOfArray = oneOfArray
                            )
                            ClassName(packageName, sealedInterfaceName)
                        }
                        arrayType.isNotEmpty() -> {
                            mapSwaggerTypeToKotlin(arrayType)
                        }
                        else -> null
                    }
                    if (resolvedType != null) {
                        appendClassProperty(
                            classBuilder = classBuilder,
                            propertyName = propertyName,
                            // TODO implement
                            nullable = false,
                            description = propertyDescription,
                            example = propertyExample,
                            resolvedType = resolvedType
                        )
                    }
                }

                propertyOneOf -> {
                    val sealedInterfaceName = "${propertyName.capitalized()}Interface"
                    val oneOfArray = propertyObject.getJSONArray("oneOf")
                    generateSealedInterfacePropertyUseCase(sealedInterfaceName, oneOfArray)
                    // And append a property
                    appendClassProperty(
                        classBuilder = classBuilder,
                        propertyName = propertyName,
                        // TODO implement
                        nullable = false,
                        description = propertyDescription,
                        example = propertyExample,
                        resolvedType = ClassName(packageName, sealedInterfaceName)
                    )
                }
                propertyRef -> {
                    handleReferenceProperty(
                        classBuilder = classBuilder,
                        propertyName = propertyName,
                        propertyType = propertyObject.getString("\$ref").getReferenceClassName(),
                        propertyDescription = propertyDescription,
                        propertyExample = propertyExample
                    )
                }
                else -> {
                    handlePropertyTypes(
                        propertyObject = propertyObject,
                        propertyName = propertyName,
                        propertyType = propertyType,
                        propertyDescription = propertyDescription,
                        propertyExample = propertyExample,
                        classBuilder = classBuilder
                    )
                }
            }
        }
    }

    private fun handleReferenceProperty(
        classBuilder: TypeSpec.Builder,
        propertyName: String,
        propertyType: String,
        propertyDescription: String,
        propertyExample: String
    ) {
        appendClassProperty(
            classBuilder = classBuilder,
            propertyName = propertyName,
            // TODO implement
            nullable = false,
            description = propertyDescription,
            example = propertyExample,
            resolvedType = ClassName(
                packageName,
                propertyType
            )
        )
    }

    private fun handlePropertyTypes(
        propertyObject: JSONObject,
        propertyName: String,
        propertyType: String,
        propertyDescription: String,
        propertyExample: String,
        classBuilder: TypeSpec.Builder
    ) {
        when (propertyType) {
            "string" -> {
                val resolvedType = if (propertyObject.has("enum")) {
                    val enumClassName = "${propertyName.capitalized()}Option"
                    generateEnum(
                        enumName = enumClassName,
                        propertyObject = propertyObject
                    )
                    ClassName(packageName, enumClassName)
                } else {
                    STRING
                }
                appendClassProperty(
                    classBuilder = classBuilder,
                    propertyName = propertyName,
                    // TODO handle nullable
                    nullable = false,
                    description = propertyDescription,
                    example = propertyExample,
                    resolvedType = resolvedType
                )
            }

            else -> {
                try {
                    val type = mapSwaggerTypeToKotlin(type = propertyType)
                    appendClassProperty(
                        classBuilder = classBuilder,
                        propertyName = propertyName,
                        // TODO handle nullable
                        nullable = false,
                        description = propertyDescription,
                        example = propertyExample,
                        resolvedType = type
                    )
                } catch (e: Exception) {
                    throw Exception("Class name: $propertyName")
                }
            }
        }
    }

    private fun generateEnum(
        enumName: String,
        propertyObject: JSONObject
    ) {
        val enumFileSpec = enumClassGenerator(enumName)
        val file = fileGenerator(enumName) {
            propertyObject.getJSONArray("enum").mapNotNull { enumProperty ->
                val property = enumProperty as String
                appendEnumProperty(enumFileSpec, property)
            }
            listOf(enumFileSpec.build())
        }
        fileWriter.write(file.build())
    }

    private fun mapSwaggerTypeToKotlin(
        type: String
    ): TypeName {
        return when (type) {
            "string" -> STRING
            "integer" -> INT
            "number" -> DOUBLE
            "boolean" -> BOOLEAN
            else -> throw IllegalStateException("Unsupported: $type")
        }
    }
}
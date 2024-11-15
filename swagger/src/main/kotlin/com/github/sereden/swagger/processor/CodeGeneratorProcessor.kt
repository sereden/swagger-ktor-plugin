package com.github.sereden.swagger.processor

import com.github.sereden.swagger.plugin.SwaggerExtensions
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.gradle.api.Project
import org.gradle.configurationcache.extensions.capitalized
import org.json.JSONArray
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
        val outputDir = File("${target.projectDir}/build/generated/ksp/metadata/commonMain/kotlin")
        outputDir.mkdirs()
        val jsonObject = JSONObject(swaggerFile.readText())
        val schemas = jsonObject
            .getJSONObject("components")
            .optJSONObject("schemas") ?: JSONObject()

        // Generate a Kotlin data class for each schema in the Swagger JSON
        schemas.keys().forEach { schemaName ->
            val schemaObject = schemas.getJSONObject(schemaName)
            val fileSpec = generateKotlinDataClass(
                packageName = packageName,
                className = schemaName,
                schemaObject = schemaObject,
                schemas = schemas,
                outputDir = outputDir
            )
            println("Generated")
            fileSpec.writeTo(outputDir)
        }
    }

    private fun generateKotlinDataClass(
        packageName: String,
        className: String,
        schemaObject: JSONObject,
        schemas: JSONObject,
        outputDir: File
    ): FileSpec {
        val classBuilder = TypeSpec.classBuilder(className)
            .addAnnotation(Serializable::class)
        iterateProperties(
            schemaObject = schemaObject,
            packageName = packageName,
            className = className,
            classBuilder = classBuilder,
            schemas = schemas,
            outputDir = outputDir
        )

        val kotlinClass = classBuilder
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .apply {
                        classBuilder.propertySpecs.forEach { property ->
                            addParameter(property.name, property.type)
                        }
                    }
                    .build()
            )
            .build()
        return FileSpec.builder(packageName, className)
            .addType(kotlinClass)
            .build()
    }

    private fun iterateProperties(
        outputDir: File,
        schemaObject: JSONObject,
        packageName: String,
        className: String,
        classBuilder: TypeSpec.Builder,
        schemas: JSONObject,
        optional: Boolean = false
    ) {
        // Get properties for the schema and generate each property
        val properties = getProperties(schemaObject)
        val allArray = schemaObject.optJSONArray("allOf") ?: JSONArray()
        val oneOfArray = schemaObject.optJSONArray("oneOf") ?: JSONArray()
        if (!allArray.isEmpty) {
            allArray.forEach { allArrayItem ->
                val allJsonItem = allArrayItem as JSONObject
                val nestedProperties = getProperties(allArrayItem)
                if (!nestedProperties.isEmpty) {
                    iterateProperties(
                        schemaObject = allArrayItem,
                        packageName = packageName,
                        className = className,
                        classBuilder = classBuilder,
                        schemas = schemas,
                        outputDir = outputDir
                    )
                } else if (getReferenceClass(allJsonItem) != null) {
                    val reference = getReferenceClass(allJsonItem)
                    iterateProperties(
                        schemaObject = schemas.optJSONObject(reference),
                        packageName = packageName,
                        className = className,
                        classBuilder = classBuilder,
                        schemas = schemas,
                        outputDir = outputDir
                    )
                }
            }
        } else if (!oneOfArray.isEmpty) {
            iterateOneOfProperties(
                oneOfArray = oneOfArray,
                packageName = packageName,
                className = className,
                classBuilder = classBuilder,
                schemas = schemas,
                outputDir = outputDir
            )
        } else {
            generateClassProperties(
                properties = properties,
                packageName = packageName,
                className = className,
                classBuilder = classBuilder,
                schemas = schemas,
                outputDir = outputDir,
                optional = optional
            )
        }
    }

    private fun iterateOneOfProperties(
        oneOfArray: JSONArray,
        packageName: String,
        className: String,
        classBuilder: TypeSpec.Builder,
        schemas: JSONObject,
        outputDir: File
    ) {
        oneOfArray.forEach { oneOfArrayItem ->
            val oneOfJsonItem = oneOfArrayItem as JSONObject
            val nestedProperties = getProperties(oneOfArrayItem)
            if (!nestedProperties.isEmpty) {
                iterateProperties(
                    schemaObject = nestedProperties,
                    packageName = packageName,
                    className = className,
                    classBuilder = classBuilder,
                    schemas = schemas,
                    outputDir = outputDir,
                    optional = true
                )
            } else if (getReferenceClass(oneOfJsonItem) != null) {
                val reference = getReferenceClass(oneOfJsonItem)
                if (reference != className) {
                    iterateProperties(
                        schemaObject = schemas.optJSONObject(reference),
                        packageName = packageName,
                        className = className,
                        classBuilder = classBuilder,
                        schemas = schemas,
                        outputDir = outputDir,
                        optional = true
                    )
                }
            }
        }
    }

    private fun getProperties(schemaObject: JSONObject?) = schemaObject?.optJSONObject("properties") ?: JSONObject()

    private fun generateClassProperties(
        outputDir: File,
        properties: JSONObject,
        packageName: String,
        className: String,
        classBuilder: TypeSpec.Builder,
        schemas: JSONObject,
        optional: Boolean
    ) {
        properties.keys().forEach { propertyName ->
            val propertyObject = properties.getJSONObject(propertyName)
            val propertyType = propertyObject.optString("type")
            val propertyItems = propertyObject.optJSONObject("items")
            val propertyRef = getReferenceClass(propertyObject)
            val propertyOneOf = propertyObject.optJSONArray("oneOf")
            // If there is a $ref, resolve it to the referenced schema
            val resolvedType = if (propertyRef != null) {
                val refSchemaName = propertyRef.substringAfterLast("/")
                ClassName(packageName, refSchemaName) // Reference to generated class
            } else if (propertyOneOf != null) {
                iterateOneOfProperties(
                    oneOfArray = propertyOneOf,
                    packageName = packageName,
                    className = className,
                    classBuilder = classBuilder,
                    schemas = schemas,
                    outputDir = outputDir
                )
                null
            } else {
                when (propertyType) {
                    "object" -> {
                        // Generate ObjectFile
                        val className = "${propertyName.capitalized()}ObjectClass"
                        generateKotlinDataClass(
                            packageName = packageName,
                            className = className,
                            schemaObject = propertyObject,
                            schemas = schemas,
                            outputDir = outputDir
                        ).apply {
                            writeTo(outputDir)
                        }
                        ClassName(packageName, className)
                    }

                    "array" -> {
                        if (getReferenceClass(propertyItems!!) != null) {
                            getReferenceClass(propertyItems)
                            LIST.parameterizedBy(ClassName(packageName, getReferenceClass(propertyItems)!!))
                        } else {
                            LIST.parameterizedBy(
                                mapSwaggerTypeToKotlin(
                                    type = propertyItems.optString("type"),
                                )
                            )
                        }
                    }

                    "string" -> {
                        if (propertyObject.has("enum")) {
                            val enumClassName = "${propertyName.capitalized()}Options"
                            generateEnum(
                                packageName = packageName,
                                enumName = enumClassName,
                                propertyObject = propertyObject,
                                outputDir = outputDir
                            )
                            ClassName(packageName, enumClassName)
                        } else {
                            STRING
                        }
                    }

                    else -> mapSwaggerTypeToKotlin(
                        type = propertyType
                    )
                }
            }
            if (resolvedType != null) {
                val propertySpec = PropertySpec.builder(
                    propertyName,
                    resolvedType.copy(nullable = optional)
                ).addAnnotation(
                    AnnotationSpec.builder(SerialName::class)
                        .addMember("%S", propertyName)
                        .build()
                ).initializer(propertyName)
                    .build()

                classBuilder.addProperty(propertySpec)
            }
        }
    }

    private fun generateEnum(
        packageName: String,
        enumName: String,
        propertyObject: JSONObject,
        outputDir: File
    ) {
        val classBuilder = TypeSpec.enumBuilder(enumName)
            .addAnnotation(Serializable::class)

        propertyObject.getJSONArray("enum").forEach { enumProperty ->
            val property = enumProperty as String

            classBuilder.addEnumConstant(
                property, TypeSpec.anonymousClassBuilder()
                    .addAnnotation(
                        AnnotationSpec.builder(SerialName::class)
                            .addMember("%S", property)
                            .build()
                    )
                    .build()
            )
        }
        FileSpec.builder(packageName, enumName)
            .addType(classBuilder.build())
            .build().apply {
                writeTo(outputDir)
            }
    }

    private fun getReferenceClass(jsonObject: JSONObject): String? {
        val propertyRef = jsonObject.optString("\$ref")
        return if (propertyRef.isNotEmpty()) {
            propertyRef.substringAfterLast("/").ifBlank { null }
        } else {
            null
        }
    }

    private fun mapSwaggerTypeToKotlin(
        type: String
    ): TypeName {
        return when (type) {
            "string" -> STRING
            "integer" -> INT
            "number" -> DOUBLE
            "boolean" -> BOOLEAN
            else -> {
                throw IllegalStateException("Unsupported: $type")
            }
        }
    }
}

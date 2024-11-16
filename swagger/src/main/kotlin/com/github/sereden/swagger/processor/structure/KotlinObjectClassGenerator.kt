package com.github.sereden.swagger.processor.structure

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

class KotlinObjectClassGenerator {

    operator fun invoke(
        className: String,
        options: List<String>
    ): TypeSpec.Builder {
        return TypeSpec.objectBuilder("${className}Serializer")
            .addSuperinterface(
                ClassName("kotlinx.serialization", "KSerializer")
                    .parameterizedBy(ClassName("", className))
            )
            // Add descriptor property
            .addProperty(
                PropertySpec.builder("descriptor", ClassName("kotlinx.serialization.descriptors", "SerialDescriptor"))
                    .addModifiers(KModifier.OVERRIDE)
                    .initializer(
                        "%T(%S)",
                        ClassName("kotlinx.serialization.descriptors", "buildClassSerialDescriptor"),
                        className
                    )
                    .build()
            )
            .addFunction(
                FunSpec.builder("serialize")
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter("encoder", ClassName("kotlinx.serialization.encoding", "Encoder"))
                    .addParameter("value", ClassName("", className))
                    .addCode(
                        buildString {
                            appendLine("when (value) {")
                            options.forEach { option ->
                                appendLine(
                                    "    is ${option}Option -> encoder.encodeSerializableValue($option.serializer(), value.value)"
                                )
                            }
                            appendLine("}")
                        }
                    )
                    .returns(Unit::class)
                    .build()
            )
            // Add deserialize function
            .addFunction(
                FunSpec.builder("deserialize")
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter("decoder", ClassName("kotlinx.serialization.encoding", "Decoder"))
                    .returns(ClassName("", className))
                    .addCode(
                        """
                        val input = decoder as? %T 
                            ?: throw %T("This class can only be used with Json")
                        val jsonElement = input.decodeJsonElement()
                        val jsonObject = jsonElement.jsonObject
                        // TODO Requires performance improvement - use a single instance of Json 
                        val json = Json { 
                            // Required to ensure that find an appropriate sealed interface
                            ignoreUnknownKeys = false
                        } 
                        
                        """.trimIndent(),
                        ClassName("kotlinx.serialization.json", "JsonDecoder"),
                        ClassName("kotlinx.serialization", "SerializationException")
                    ).apply {
                        // Add try-catch blocks for each option
                        options.forEachIndexed { index, option ->
                            val isLastOption = index == options.lastIndex
                            addCode(
                                if (isLastOption) {
                                    """
                                return ${option}Option(
                                    value = json.decodeFromJsonElement($option.serializer(), jsonObject)
                                )
                                """.trimIndent()
                                } else {
                                    """
                                    try {
                                        return ${option}Option(
                                            value = json.decodeFromJsonElement($option.serializer(), jsonObject)
                                        )
                                    } catch (e: Exception) {
                                """.trimIndent()
                                }
                            )
                        }

                        // Close all try-catch blocks
                        repeat(options.size - 1) {
                            addCode("}\n")
                        }
                    }.build()
            )
    }
}
package com.github.sereden.swagger.processor

import com.github.sereden.swagger.processor.file.FileManager
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asClassName

class ProcessEnumSerializer(
    private val packageName: String,
    private val fileManager: FileManager,
    private val kotlinFileGenerator: KotlinFileGenerator,
) {
    fun process() {
        kotlinFileGenerator(INTERFACE_NAME) {
            listOf(
                generateInterface(),
                generateImplementation()
            )
        }.apply {
            fileManager.write(build())
        }
    }

    private fun generateInterface(): TypeSpec {
        // Create the property for the interface
        val serialNameProperty = PropertySpec.builder("serialName", String::class)
            .addModifiers(KModifier.ABSTRACT)
            .build()

        val fromSerialNameFunction = FunSpec.builder("fromSerialName")
            .addModifiers(KModifier.INLINE)
            .addTypeVariable(
                TypeVariableName(
                    name = "T",
                    bounds = listOf(
                        ClassName("kotlin", "Enum").parameterizedBy(TypeVariableName("T")),
                        ClassName(packageName, "FallbackEnum")
                    )
                )
            )
            .addParameter("value", String::class)
            .addParameter(
                "values",
                Array::class.asClassName().parameterizedBy(TypeVariableName("T"))
            )
            .addParameter("fallback", TypeVariableName("T"))
            .returns(TypeVariableName("T"))
            .addStatement(
                "return values.find { it.serialName == value } ?: fallback"
            )
            .build()

        // Create the companion object
        val companionObject = TypeSpec.companionObjectBuilder()
            .addFunction(fromSerialNameFunction)
            .build()

        // Build the interface
        return TypeSpec.interfaceBuilder("FallbackEnum")
            .addProperty(serialNameProperty)
            .addType(companionObject)
            .build()
    }

    private fun generateImplementation(): TypeSpec {
        // Type variable T with constraints
        val typeVariableT = TypeVariableName("T")
            .copy(bounds = listOf(
                ClassName("kotlin", "Enum").parameterizedBy(TypeVariableName("T")),
                ClassName("", "FallbackEnum")
            ))

        // Constructor parameters
        val enumValuesParam = ParameterSpec.builder(
            "enumValues", Array::class.asClassName().parameterizedBy(typeVariableT)
        ).build()

        val fallbackParam = ParameterSpec.builder("fallback", typeVariableT).build()

        // Property: descriptor
        val descriptorProperty = PropertySpec.builder(
            "descriptor", ClassName("kotlinx.serialization.descriptors", "SerialDescriptor")
        )
            .addModifiers(KModifier.OVERRIDE)
            .initializer(
                "%T(%S, %T)",
                ClassName("kotlinx.serialization.descriptors", "PrimitiveSerialDescriptor"),
                "FallbackEnum",
                ClassName("kotlinx.serialization.descriptors.PrimitiveKind", "STRING")
            )
            .build()

        // Function: serialize
        val serializeFunction = FunSpec.builder("serialize")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("encoder", ClassName("kotlinx.serialization.encoding", "Encoder"))
            .addParameter("value", typeVariableT)
            .addStatement("encoder.encodeString(value.serialName)")
            .build()

        // Function: deserialize
        val deserializeFunction = FunSpec.builder("deserialize")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("decoder", ClassName("kotlinx.serialization.encoding", "Decoder"))
            .returns(typeVariableT)
            .addStatement("val value = decoder.decodeString()")
            .addStatement(
                "return %T.fromSerialName(value, enumValues, fallback)",
                ClassName("", "FallbackEnum")
            )
            .build()

        // Build the abstract class
        return TypeSpec.classBuilder("FallbackEnumSerializer")
            .addModifiers(KModifier.ABSTRACT)
            .addTypeVariable(typeVariableT)
            .addSuperinterface(
                ClassName("kotlinx.serialization", "KSerializer")
                    .parameterizedBy(typeVariableT)
            )
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(enumValuesParam)
                    .addParameter(fallbackParam)
                    .build()
            )
            .addProperty(enumValuesParam.toPropertySpec(KModifier.PRIVATE))
            .addProperty(fallbackParam.toPropertySpec(KModifier.PRIVATE))
            .addProperty(descriptorProperty)
            .addFunction(serializeFunction)
            .addFunction(deserializeFunction)
            .build()
    }

    // Extension function to convert a ParameterSpec to a PropertySpec
    private fun ParameterSpec.toPropertySpec(modifier: KModifier): PropertySpec {
        return PropertySpec.builder(name, type)
            .addModifiers(modifier)
            .initializer(name)
            .build()
    }

    companion object {
        const val INTERFACE_NAME = "FallbackEnum"
    }
}
package com.github.sereden.swagger.processor.structure

import com.github.sereden.swagger.processor.ProcessEnumSerializer
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

class KotlinEnumClassGenerator(
    private val packageName: String
) {
    operator fun invoke(
        className: String
    ): TypeSpec.Builder {
        val serializableAnnotation = AnnotationSpec.builder(
            type = ClassName("kotlinx.serialization", "Serializable")
        ).addMember("with = %T::class", ClassName(packageName, "${className}.Serializer"))
            .build()

        val serializerObject = TypeSpec.objectBuilder("Serializer")
            .superclass(
                ClassName(packageName, "FallbackEnumSerializer")
                    .parameterizedBy(ClassName(packageName, className))
            )
            .addSuperclassConstructorParameter("enumValues = entries.toTypedArray()")
            .addSuperclassConstructorParameter("fallback = Unknown")
            .build()
        return TypeSpec.enumBuilder(className)
            .addAnnotation(serializableAnnotation)
            .primaryConstructor(
                // Create a property that gets the original value
                FunSpec.constructorBuilder()
                    .addParameter(
                        ParameterSpec.builder(SERIAL_NAME_PROPERTY, String::class)
                            .build()
                    )
                    .build()
            )
            .addSuperinterface(ClassName(packageName, ProcessEnumSerializer.INTERFACE_NAME))
            .addProperty(
                PropertySpec.builder(SERIAL_NAME_PROPERTY, String::class)
                    .initializer(SERIAL_NAME_PROPERTY)
                    .addModifiers(KModifier.OVERRIDE)
                    .build()
            )
            .addType(serializerObject)
    }

    companion object {
        private const val SERIAL_NAME_PROPERTY = "serialName"
    }
}
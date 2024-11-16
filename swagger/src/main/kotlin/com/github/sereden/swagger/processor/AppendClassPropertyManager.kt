package com.github.sereden.swagger.processor

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import kotlinx.serialization.SerialName

class AppendClassPropertyManager {
    operator fun invoke(
        classBuilder: TypeSpec.Builder,
        propertyName: String,
        nullable: Boolean,
        description: String,
        example: String,
        resolvedType: TypeName
    ) {
        val existingPropertySpec = classBuilder.propertySpecs.find { spec -> spec.name == propertyName }
        if (existingPropertySpec == null) {
            val propertySpec = PropertySpec.builder(
                propertyName,
                resolvedType.copy(nullable = nullable)
            ).apply {
                if (description.isNotEmpty()) {
                    addKdoc(description)
                        .addKdoc("\n")
                }
                if (example.isNotBlank()) {
                    addKdoc("Example:\n```$example```")
                }
            }.addAnnotation(
                AnnotationSpec.builder(SerialName::class)
                    .addMember("%S", propertyName)
                    .build()
            ).initializer(propertyName)
                .build()

            classBuilder.addProperty(propertySpec)
        } else {
            println(
                "Property $propertyName has already been defined in class." +
                        "The existing type is: ${existingPropertySpec.type}. Added type: $resolvedType"
            )
        }
    }
}
package com.github.sereden.swagger.processor.structure

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec

class KotlinSealedInterfaceGenerator(private val packageName: String) {
    operator fun invoke(
        className: String
    ): TypeSpec {
        val serializableAnnotation = AnnotationSpec.builder(ClassName("kotlinx.serialization", "Serializable"))
            .addMember("with = %T::class", ClassName(packageName, "${className}Serializer"))
            .build()
        return TypeSpec.interfaceBuilder(className)
            .addModifiers(KModifier.SEALED)
            .addAnnotation(serializableAnnotation)
            .build()
    }
}
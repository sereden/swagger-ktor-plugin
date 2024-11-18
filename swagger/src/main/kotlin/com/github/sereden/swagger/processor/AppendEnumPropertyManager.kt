package com.github.sereden.swagger.processor

import com.github.sereden.swagger.utils.toCamelCase
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.TypeSpec
import kotlinx.serialization.SerialName

class AppendEnumPropertyManager {
    operator fun invoke(
        classBuilder: TypeSpec.Builder,
        propertyName: String,
    ) {
        classBuilder.addEnumConstant(
            propertyName.toCamelCase(), TypeSpec.anonymousClassBuilder()
                .addAnnotation(
                    AnnotationSpec.builder(SerialName::class)
                        .addMember("%S", propertyName)
                        .build()
                )
                .addSuperclassConstructorParameter("%S", propertyName)
                .build()
        )
    }
}
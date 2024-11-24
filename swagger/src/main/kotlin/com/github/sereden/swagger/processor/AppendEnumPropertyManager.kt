package com.github.sereden.swagger.processor

import com.github.sereden.swagger.utils.toCamelCase
import com.squareup.kotlinpoet.TypeSpec

class AppendEnumPropertyManager {
    operator fun invoke(
        classBuilder: TypeSpec.Builder,
        propertyName: String,
    ) {
        classBuilder.addEnumConstant(
            propertyName.toCamelCase(), TypeSpec.anonymousClassBuilder()
                .addSuperclassConstructorParameter("%S", propertyName)
                .build()
        )
    }
}
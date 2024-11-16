package com.github.sereden.swagger.processor.structure

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import kotlinx.serialization.Serializable

class KotlinEnumClassGenerator {
    operator fun invoke(
        className: String
    ): TypeSpec.Builder {
        return TypeSpec.enumBuilder(className)
            .addAnnotation(Serializable::class)
            .primaryConstructor(
                // Create a property that gets the original value
                FunSpec.constructorBuilder()
                    .addParameter(
                        ParameterSpec.builder(RAW_VALUE_PROPERTY, String::class)
                            .build()
                    )
                    .build()
            )
            .addProperty(
                PropertySpec.builder(RAW_VALUE_PROPERTY, String::class)
                    .initializer(RAW_VALUE_PROPERTY)
                    .build()
            )
    }

    companion object {
        private const val RAW_VALUE_PROPERTY = "rawValue"
    }
}
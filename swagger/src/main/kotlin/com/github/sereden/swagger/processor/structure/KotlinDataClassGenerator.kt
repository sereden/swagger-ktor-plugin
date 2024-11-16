package com.github.sereden.swagger.processor.structure

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import kotlinx.serialization.Serializable

class KotlinDataClassGenerator {
    operator fun invoke(
        className: String,
        write: (classBuilder: TypeSpec.Builder) -> Unit
    ): TypeSpec.Builder {
        val classBuilder = TypeSpec.classBuilder(className)
            .addAnnotation(Serializable::class)

        write(classBuilder)
        return classBuilder
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .apply {
                        classBuilder.propertySpecs.forEach { property ->
                            addParameter(property.name, property.type)
                        }
                    }
                    .build()
            )
    }
}
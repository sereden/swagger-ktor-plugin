package com.github.sereden.swagger.plugin

import com.github.sereden.swagger.processor.CodeGeneratorProcessor
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.gradle.language.assembler.tasks.Assemble

class GenerateCodeFromSwaggerPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val extension = target.extensions.create("swagger", SwaggerExtensions::class.java)
        target.tasks.register("generateCodeFromSwaggerTask") {
            group = "Code generation"
            description = "Generates Kotlin data classes from Swagger API schema"

            doLast {
                CodeGeneratorProcessor(target, extension).process()
            }
        }
        target.tasks.withType<Assemble>{
            dependsOn("generateCodeFromSwaggerTask")
        }
    }
}


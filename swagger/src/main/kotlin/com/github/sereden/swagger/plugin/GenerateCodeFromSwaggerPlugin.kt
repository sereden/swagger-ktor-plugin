package com.github.sereden.swagger.plugin

import com.github.sereden.swagger.download.DownloadSwaggerJsonTask
import com.github.sereden.swagger.processor.CodeGeneratorProcessor
import org.gradle.api.Plugin
import org.gradle.api.Project

class GenerateCodeFromSwaggerPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val extension = target.extensions.create("swagger", SwaggerExtensions::class.java)
        val generateTask = target.tasks.register("generateCodeFromSwaggerTask") {
            group = "Swagger DTO generation"
            description = "Generates Kotlin data classes from Swagger API schema"

            doLast {
                CodeGeneratorProcessor(target, extension).process()
            }
        }

        target.tasks.register("downloadSwaggerScheme") {
            group = "Swagger DTO generation"
            description = "Generates at places Swagger JSON"

            doLast {
                DownloadSwaggerJsonTask(extension).execute()
            }
        }

        target.tasks.configureEach {
            if (name.startsWith("assemble")) {
                dependsOn(generateTask)
            }
        }
    }
}


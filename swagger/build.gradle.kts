plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.kotlinpoet)
    implementation(libs.json)
    implementation(libs.kotlinx.serialization)
}

kotlin {
    jvmToolchain(JavaVersion.VERSION_17.toString().toInt())
}

gradlePlugin {
    plugins {
        register("generateCodeFromSwagger") {
            id = "com.github.sereden.swagger"
            implementationClass = "com.github.sereden.swagger.plugin.GenerateCodeFromSwaggerPlugin"
        }
    }
}

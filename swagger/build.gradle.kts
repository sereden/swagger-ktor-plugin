plugins {
    `kotlin-dsl`
    `maven-publish`
}

dependencies {
    implementation(libs.kotlinpoet)
    implementation(libs.json)
    implementation(libs.kotlinx.serialization)
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(JavaVersion.VERSION_17.toString().toInt())
}

tasks.test {
    useJUnitPlatform()
}

gradlePlugin {
    plugins {
        register("generateCodeFromSwagger") {
            id = "io.github.sereden.swagger"
            implementationClass = "com.github.sereden.swagger.plugin.GenerateCodeFromSwaggerPlugin"
        }
    }
}

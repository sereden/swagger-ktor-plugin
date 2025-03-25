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

version = "1.0.0"
group = "io.github.sereden"

gradlePlugin {
    plugins {
        create("generateCodeFromSwagger") {
            id = "io.github.sereden.swagger-ktor-plugin"
            implementationClass = "com.github.sereden.swagger.plugin.GenerateCodeFromSwaggerPlugin"
        }
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/sereden/swagger-ktor-plugin")
            credentials {
                username = project.findProperty("GITHUB_USER") as? String ?: System.getenv("GITHUB_USER")
                password = project.findProperty("GITHUB_TOKEN") as? String ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
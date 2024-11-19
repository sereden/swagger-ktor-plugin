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

publishing {
    publications {
        register<MavenPublication>("gpr") {
            from(components["java"])
        }
    }
    // TODO do not publish
//    publications {
//        create<MavenPublication>("pluginMaven") {
//            groupId = "io.github.sereden"
//            artifactId = "swagger"
//            version = "1.0.0"
//
//            // Customize POM if needed
//            pom {
//                name.set("Swagger to kotlinx DTO generator")
//                description.set("Generates Kotlin data classes from Swagger API schema")
//                licenses {
//                    license {
//                        name.set("The Apache License, Version 2.0")
//                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
//                    }
//                }
//                developers {
//                    developer {
//                        id.set("sereden")
//                        name.set("Andrii Seredenko")
//                        email.set("a.seredenko@gmail.com")
//                    }
//                }
//                scm {
//                    connection.set("scm:git:ssh://github.com/sereden/swagger-ktor-plugin.git")
//                    developerConnection.set("scm:git:ssh://github.com/sereden/swagger-ktor-plugin.git")
//                    url.set("https://github.com/sereden/swagger-ktor-plugin")
//                }
//            }
//        }
//    }

    repositories {
        mavenLocal()
        maven {
            name = "Github"
            url = uri("https://maven.pkg.github.com/sereden/swagger-ktor-plugin")
            credentials {
//                username = project.findProperty("SWAGGER_SONATYPE_USER") as String? ?: System.getenv("SWAGGER_SONATYPE_USER")
//                password = project.findProperty("SWAGGER_SONATYPE_PASSWORD") as String? ?: System.getenv("SWAGGER_SONATYPE_PASSWORD")
            }
        }
    }
}
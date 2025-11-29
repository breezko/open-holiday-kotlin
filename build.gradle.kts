import org.gradle.api.tasks.bundling.Zip

plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.serialization") version "2.2.21"
    `maven-publish`
    signing
}

group = "io.github.breezko"
version = "2.0.0"

subprojects {
    group = rootProject.group
    version = rootProject.version

    repositories {
        mavenCentral()
    }

    plugins.apply("org.jetbrains.kotlin.jvm")
    plugins.apply("org.jetbrains.kotlin.plugin.serialization")
    plugins.apply("org.gradle.maven-publish")
    plugins.apply("org.gradle.signing")

    kotlin {
        jvmToolchain(17)
    }

    java {
        withSourcesJar()
        withJavadocJar()
    }


    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
                artifactId = when (project.name) {
                    "core" -> "open-holiday-kotlin-core"
                    "ktor-client" -> "open-holiday-kotlin-ktor"
                    "spring-client" -> "open-holiday-kotlin-spring"
                    else -> project.name
                }

                pom {
                    name.set(
                        when (project.name) {
                            "core" -> "Open Holiday Kotlin Client - Core"
                            "ktor-client" -> "Open Holiday Kotlin Client - Ktor"
                            "spring-webclient" -> "Open Holiday Kotlin Client - Spring WebClient"
                            else -> "Open Holiday Kotlin Client"
                        }
                    )
                    description.set("Kotlin client library for Open Holiday API (${project.name})")
                    url.set("https://github.com/breezko/open-holiday-kotlin")

                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://opensource.org/licenses/MIT")
                        }
                    }

                    developers {
                        developer {
                            id.set("breezko")
                            name.set("Hendrik Heim")
                            email.set("hendrik@breezko.dev")
                        }
                    }

                    scm {
                        connection.set("scm:git:git://github.com/breezko/open-holiday-kotlin.git")
                        developerConnection.set("scm:git:ssh://git@github.com:breezko/open-holiday-kotlin.git")
                        url.set("https://github.com/breezko/open-holiday-kotlin")
                    }
                }
            }
        }

        repositories {
            maven {
                name = "centralPortal"
                url = uri(layout.buildDirectory.dir("central-portal-repo"))
            }
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/breezko/open-holiday-kotlin")
                credentials {
                    username = System.getenv("GITHUB_ACTOR")
                    password = System.getenv("GITHUB_TOKEN")
                }
            }
        }
    }

    signing {
        useGpgCmd()
        sign(publishing.publications["mavenJava"])
    }
}


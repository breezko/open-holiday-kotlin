import org.gradle.api.tasks.bundling.Zip

plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.serialization") version "2.2.21"
    `maven-publish`
    signing
}

group = "io.github.breezko"
version = "1.3.0"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    // HTTP client - Ktor implementation
    implementation("io.ktor:ktor-client-core:3.1.1")
    implementation("io.ktor:ktor-client-cio:3.1.1")
    implementation("io.ktor:ktor-client-content-negotiation:3.1.1")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.1.1")
    implementation("io.ktor:ktor-client-logging:3.1.1")

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
    implementation("org.springframework:spring-webflux:6.2.14")

    // DateTime for Ktor serialization support
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")

    // Spring WebFlux - Spring implementation
    implementation("org.springframework:spring-context:6.2.14")
    implementation("io.projectreactor:reactor-core:3.7.0")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:1.2.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.9.0")

    // Jackson for Spring WebClient JSON processing
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.18.2")
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.register<Zip>("packageForSonatype") {
    group = "publishing"
    description = "Packages artifacts for Sonatype Central Portal upload"

    dependsOn("publishMavenJavaPublicationToCentralPortalRepository")

    val artifactVersion = version.toString()
    val repoDir = layout.buildDirectory.dir("central-portal-repo")

    from(repoDir) {
        include("io/github/breezko/open-holiday-kotlin/$artifactVersion/**")
    }

    archiveFileName.set("open-holiday-kotlin-$artifactVersion.zip")
    destinationDirectory.set(layout.buildDirectory)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "open-holiday-kotlin"

            pom {
                name.set("Open Holiday Kotlin Client")
                description.set("Kotlin client library for Open Holiday API")
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

        // Optional: keep GitHub Packages
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

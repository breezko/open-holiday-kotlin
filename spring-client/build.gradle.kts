dependencies {
    api(project(":core"))

    api("org.springframework:spring-webflux:6.2.14")
    api("org.springframework:spring-context:6.2.14")
    api("io.projectreactor:reactor-core:3.7.0")
    api("io.projectreactor.kotlin:reactor-kotlin-extensions:1.2.3")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.9.0")

    api("com.fasterxml.jackson.core:jackson-databind:2.18.2")
    api("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.2")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.18.2")
}

plugins {
    kotlin("jvm") version "2.2.21"
    application
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("io.github.breezko:open-holiday-kotlin-ktor:1.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
}

application {
    mainClass.set("org.openholidays.KtorSampleKt")
}

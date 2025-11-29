plugins {
    kotlin("jvm") version "2.2.21"
    application
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("io.github.breezko:open-holiday-kotlin-spring:1.3.0")
}

application {
    mainClass.set("org.openholidays.SpringSampleKt")
}

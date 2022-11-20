import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.21"
    application
    id("io.gitlab.arturbosch.detekt") version "1.21.0"
}

group = "com._7aske"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    detekt("io.gitlab.arturbosch.detekt:detekt-formatting:1.21.0")
    detekt("io.gitlab.arturbosch.detekt:detekt-cli:1.21.0")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("com._7aske.strapparser.MainKt")
}
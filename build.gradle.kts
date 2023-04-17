import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.20"
    application
    id("io.gitlab.arturbosch.detekt") version "1.21.0"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

val exports = listOf(
    "--add-exports", "jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
    "--add-exports", "jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED",
    "--add-exports", "jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED",
    "--add-exports", "jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
    "--add-exports", "jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED"
)

group = "com._7aske"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    detekt("io.gitlab.arturbosch.detekt:detekt-formatting:1.21.0")
    detekt("io.gitlab.arturbosch.detekt:detekt-cli:1.21.0")
    implementation("com.google.googlejavaformat:google-java-format:1.15.0")
    implementation("com.xenomachina:kotlin-argparser:2.0.7")
    implementation("com.facebook:ktfmt:0.40")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
    jvmArgs = exports
}

tasks {
    val fatJar = register<Jar>("fatJar") {
        dependsOn.addAll(
            listOf(
                "compileJava",
                "compileKotlin",
                "processResources"
            )
        ) // We need this for Gradle optimization to work
        archiveClassifier.set("standalone") // Naming the jar
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        manifest { attributes(mapOf("Main-Class" to application.mainClass)) }
        val sourcesMain = sourceSets.main.get()
        val contents = configurations.runtimeClasspath.get()
            .map { if (it.isDirectory) it else zipTree(it) } +
                sourcesMain.output
        from(contents)
    }
    build {
        dependsOn(fatJar) // Trigger fat jar creation during build
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.detekt {
    autoCorrect = true
}

tasks.register("install") {
    dependsOn("shadowJar")

    doLast {
        val jar = tasks.getByName("shadowJar") as Jar
        val file = jar.archiveFile.get().asFile
        val installDir = File(System.getProperty("user.home"), ".strap-parser")
        installDir.mkdirs()
        file.copyTo(File(installDir, "strap-parser.jar"), true)
    }
}

application {
    mainClass.set("com._7aske.strapparser.MainKt")
    applicationDefaultJvmArgs = exports
}
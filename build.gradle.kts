import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "2.0.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.ertools"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    /** Serialization **/
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.5")

    /** Transmission **/
    implementation("commons-fileupload:commons-fileupload:1.5")

    /** Tests **/
    testImplementation(kotlin("test"))
}

tasks.test {
    maxHeapSize = "2G"
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(20)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(20))
    }
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("alphanumeric-recognizer")
        archiveVersion.set(version.toString())
        archiveClassifier.set("")
        mergeServiceFiles()
        manifest {
            attributes(
                "Main-Class" to "com.ertools.RunServerMainKt"
            )
        }
    }

    build {
        dependsOn(shadowJar)
    }
}

fun createTask(taskName: String, mainClassName: String) {
    tasks.register<JavaExec>(taskName) {
        group = "model tasks"
        description = "Run $mainClassName"
        mainClass.set(mainClassName)
        classpath = sourceSets["main"].runtimeClasspath
        workingDir = file("$rootDir")

        val argsProperty = project.findProperty("args")?.toString()
        if (argsProperty != null) {
            args = argsProperty.split("\\s+".toRegex())
        }
    }
}

createTask("runServer", "com.ertools.RunServerMainKt")
createTask("trainModel", "com.ertools.TrainModelMainKt")
createTask("testModel", "com.ertools.TestModelMainKt")


plugins {
    kotlin("jvm") version "2.0.0"
}

group = "com.ertools"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.5")
    implementation("commons-fileupload:commons-fileupload:1.5")
    testImplementation(kotlin("test"))
}

tasks.test {
    maxHeapSize = "2G"
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(20)
}

fun createTask(taskName: String, mainClassName: String) {
    tasks.register<JavaExec>(taskName) {
        group = "model tasks"
        description = "Run $mainClassName"
        mainClass.set(mainClassName)
        classpath = sourceSets["main"].runtimeClasspath
        workingDir = file("$rootDir")
    }
}

createTask("runServer", "com.ertools.RunServerMainKt")
createTask("trainModel", "com.ertools.TrainModelMainKt")
createTask("testModel", "com.ertools.TestModelMainKt")
/* createTask("predict", "com.ertools.model.PredictMainKt") */

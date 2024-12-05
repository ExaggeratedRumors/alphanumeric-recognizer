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
    testImplementation(kotlin("test"))
}

tasks.test {
    maxHeapSize = "2G"
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(20)
}
plugins {
    kotlin("jvm") version "2.0.0"
}

group = "com.ertools"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    maxHeapSize = "2G"
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(20)
}
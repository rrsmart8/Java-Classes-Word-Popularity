plugins {
    kotlin("jvm") version "2.0.20"
    kotlin("plugin.serialization") version "1.5.21"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(19)
}
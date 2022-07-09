plugins {
    application
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.7.0"
}

dependencies {
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kaml)
    implementation(project(":logging"))
}

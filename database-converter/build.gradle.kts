plugins {
    application
    id("java")
    kotlin("jvm")
}

dependencies {
    implementation(libs.kotlinx.serialization.core) // For the @Serializable annotation.
    implementation(libs.ts.generator)
    implementation(libs.reflections)
    implementation(libs.slf4j.nop)
}

application {
    mainClass.set("io.github.jamalam360.pinguino.database.converter.MainKt")
}

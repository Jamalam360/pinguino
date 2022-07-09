plugins {
    application
    kotlin("jvm")
}

dependencies {
    implementation(libs.groovy)
    implementation(libs.jansi)
    implementation(libs.logback)

    api(libs.logging)
}

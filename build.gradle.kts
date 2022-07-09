plugins {
    base
    kotlin("jvm") version "1.7.0" apply false
    id("org.cadixdev.licenser") version "0.6.1"
}

allprojects {
    group = "io.github.jamalam360.pinguino"
    version = "1.0.0"

    repositories {
        mavenCentral()
    }
}

dependencies {
    // Make the root project archives configuration depend on every subproject
    subprojects.forEach {
        archives(it)
    }
}

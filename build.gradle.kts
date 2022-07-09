plugins {
    application
    kotlin("jvm") version "1.7.0"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("org.cadixdev.licenser") version "0.6.1"
    id("io.gitlab.arturbosch.detekt") version "1.21.0-RC2"
}

version = "1.0.0"
group = "io.github.jamalam360.pinguino"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://maven.kotlindiscord.com/repository/maven-public/")
}

dependencies {
    subprojects.forEach {
        implementation(it)
    }
}

subprojects {
    pluginManager.apply("org.jetbrains.kotlin.jvm")

    repositories {
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://maven.kotlindiscord.com/repository/maven-public/")
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    named("build") {
        dependsOn("database-converter:run")
        dependsOn("detekt")
    }
}

application {
    // Shadow requires this. The bot should actually be run with the task bot:run, not run
    mainClass.set("io.github.jamalam360.pinguino.bot.PinguinoKt")
}

detekt {
    buildUponDefaultConfig = true
    config = rootProject.files("detekt.yml")
}

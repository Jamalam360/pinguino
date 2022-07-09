plugins {
    application
    kotlin("jvm")
}

dependencies {
    implementation(libs.kord.extensions)

    implementation(project(":config"))
    implementation(project(":logging"))
}

application {
    //TODO(Jamalam360): Replace with a CLI
    mainClass.set("io.github.jamalam360.pinguino.bot.PinguinoKt")
}

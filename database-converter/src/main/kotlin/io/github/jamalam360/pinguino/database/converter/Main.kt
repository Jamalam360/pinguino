package io.github.jamalam360.pinguino.database.converter

import kotlinx.serialization.Serializable
import me.ntrrgc.tsGenerator.TypeScriptGenerator
import org.reflections.Reflections
import java.nio.file.Path
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.writeText
import kotlin.jvm.internal.Reflection

/**
 * @author  Jamalam
 */

fun main() {
    println("Transpiling database types from Kotlin to Typescript...")
    val folder = Path.of("../site/database/types")
    folder.createDirectories()

    val reflections = Reflections("io.github.jamalam360.pinguino.database.types")
    val classes = reflections.getTypesAnnotatedWith(Serializable()).map {
        Reflection.createKotlinClass(it)
    }

    println("Discovered ${classes.size} serializable database classes.")

    val typescriptDefinition = TypeScriptGenerator(
        rootClasses = classes,
        mappings = mapOf(
            LocalDateTime::class to "Date",
            LocalDate::class to "Date"
        )
    )

    println("Writing to transpiled.d.ts...")
    val file = folder.resolve("transpiled.d.ts")
    file.deleteIfExists()
    file.createFile()
    file.writeText(typescriptDefinition.definitionsText)
}

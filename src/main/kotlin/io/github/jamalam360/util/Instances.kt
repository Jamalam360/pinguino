package io.github.jamalam360.util

import com.kotlindiscord.kord.extensions.utils.scheduling.Scheduler
import io.github.jamalam360.database.Database
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*

/**
 * @author  Jamalam360
 */

val scheduler = Scheduler()
val database = Database()
val client = HttpClient {
    install(JsonFeature)
}
val lenientClient = HttpClient {
    install(JsonFeature) {
        serializer = KotlinxSerializer(json = kotlinx.serialization.json.Json {
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
}
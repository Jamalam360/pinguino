package io.github.jamalam360.api

import io.github.jamalam360.util.lenientClient
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

/**
 * @author  Jamalam360
 */
class DadJokeApi {
    suspend fun getTheFunnyHaHa(): String {
        return lenientClient.get<DadJokeApiResponse>("https://icanhazdadjoke.com/") {
            contentType(ContentType.Application.Json)
        }.joke
    }

    @Serializable
    data class DadJokeApiResponse(val id: String, val joke: String, val status: Int)
}
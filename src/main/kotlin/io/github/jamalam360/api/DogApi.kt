package io.github.jamalam360.api

import io.github.jamalam360.util.lenientClient
import io.ktor.client.request.*
import kotlinx.serialization.Serializable

/**
 * @author  Jamalam360
 */
class DogApi {
    suspend fun getRandomDog(): String {
        return lenientClient.get<DogApiResponse>("https://dog.jamalam.tech/api/v0/breeds/image/random").message
    }

    @Serializable
    data class DogApiResponse(val message: String, val status: String)
}
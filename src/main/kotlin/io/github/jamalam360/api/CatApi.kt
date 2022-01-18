package io.github.jamalam360.api

import io.github.jamalam360.util.lenientClient
import io.ktor.client.request.*
import kotlinx.serialization.Serializable

/**
 * @author  Jamalam360
 */
class CatApi {
    suspend fun getRandomCat(): String {
        return lenientClient.get<CatApiResponse>("https://aws.random.cat/meow").file
    }

    @Serializable
    data class CatApiResponse(val file: String)

}
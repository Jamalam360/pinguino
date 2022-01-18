package io.github.jamalam360.api

import io.github.jamalam360.util.lenientClient
import io.ktor.client.request.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @author  Jamalam360
 */
class ChuckNorrisApi {
    suspend fun getExcellentChuckNorrisBasedJoke(): String {
        return lenientClient.get<ChuckNorrisApiResponse>("https://api.chucknorris.io/jokes/random").value
    }

    @Serializable
    data class ChuckNorrisApiResponse(
        @SerialName("icon_url")
        val iconUrl: String,
        val id: String,
        val url: String,
        val value: String
    )
}
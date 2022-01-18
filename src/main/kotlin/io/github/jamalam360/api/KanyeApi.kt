package io.github.jamalam360.api

import io.github.jamalam360.util.lenientClient
import io.ktor.client.request.*
import kotlinx.serialization.Serializable

/**
 * @author  Jamalam360
 */
class KanyeApi {
    suspend fun getQuote(): String {
        return lenientClient.get<KanyeApiResponse>("https://api.kanye.rest/").quote
    }

    @Serializable
    data class KanyeApiResponse(val quote: String)
}
package io.github.jamalam360.api

import io.github.jamalam360.util.lenientClient
import io.ktor.client.request.*
import kotlinx.serialization.Serializable

/**
 * @author  Jamalam360
 */
class DonaldApi {
    suspend fun getBasedOpinionsYesThisNameIsSarcastic(): String {
        return lenientClient.get<DonaldApiResponse>("https://www.tronalddump.io/random/quote").value
    }

    @Serializable
    data class DonaldApiResponse(val value: String)
}
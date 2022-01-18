package io.github.jamalam360.api

import io.github.jamalam360.util.lenientClient
import io.ktor.client.request.*
import kotlinx.serialization.Serializable

/**
 * @author  Jamalam360
 */
class BoredApi {
    suspend fun getActivity(): String {
        return lenientClient.get<BoredApiResponse>("https://www.boredapi.com/api/activity/").activity
    }

    @Serializable
    data class BoredApiResponse(
        val activity: String,
        val type: String,
        val participants: Int,
        val price: Float,
        val link: String,
        val key: String,
        val accessibility: Float
    )
}
package io.github.jamalam360.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @author  Jamalam360
 */
class ChuckNorrisApi {
    @Serializable
    data class ChuckNorrisApiResponse(
        @SerialName("icon_url")
        val iconUrl: String,
        val id: String,
        val url: String,
        val value: String
    )
}
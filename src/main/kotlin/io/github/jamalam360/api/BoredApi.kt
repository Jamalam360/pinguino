package io.github.jamalam360.api

import kotlinx.serialization.Serializable

/**
 * @author  Jamalam360
 */
class BoredApi {
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
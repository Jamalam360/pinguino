package io.github.jamalam360.api

import kotlinx.serialization.Serializable

/**
 * @author  Jamalam360
 */
class DonaldApi {
    @Serializable
    data class DonaldApiResponse(val value: String)
}
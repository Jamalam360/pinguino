package io.github.jamalam360.api

import kotlinx.serialization.Serializable

/**
 * @author  Jamalam360
 */
class KanyeApi {
    @Serializable
    data class KanyeApiResponse(val quote: String)
}
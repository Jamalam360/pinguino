package io.github.jamalam360.api

import kotlinx.serialization.Serializable

/**
 * @author  Jamalam360
 */
class CatApi {

    @Serializable
    data class CatApiResponse(val file: String)

}
package io.github.jamalam360.api

import kotlinx.serialization.Serializable

/**
 * @author  Jamalam360
 */
class DadJokeApi {

    @Serializable
    data class DadJokeApiResponse(val id: String, val joke: String, val status: Int)
}
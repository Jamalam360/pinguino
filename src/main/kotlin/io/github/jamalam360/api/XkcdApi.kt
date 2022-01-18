package io.github.jamalam360.api

import io.github.jamalam360.util.lenientClient
import io.ktor.client.request.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @author  Jamalam360
 */
class XkcdApi {
    suspend fun getComic(n: Int): XkcdApiResponse {
        return lenientClient.get(
                "https://xkcd.com/$n/info.0.json"
        )
    }

    suspend fun getLatestComic(): XkcdApiResponse {
        return lenientClient.get(
                "https://xkcd.com/info.0.json"
        )
    }

    @Serializable
    data class XkcdApiResponse(
        val month: String,
        val num: Int,
        val link: String,
        val year: String,
        val news: String,
        @SerialName("safe_title")
        val safeTitle: String,
        val transcript: String,
        val alt: String,
        val img: String,
        val title: String,
        val day: String
    )
}
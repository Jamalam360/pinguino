package io.github.jamalam360.api

import io.github.jamalam360.util.client
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

/**
 * @author  Jamalam360
 */
class LinkApi {
    suspend fun shorten(url: String): String {
        client.put<LinkAPIResponse>("https://link.jamalam.tech/api/link") {
            contentType(ContentType.Application.Json)
            body = "{\"link\": \"$url\"}"
        }.let {
            return it.link
        }
    }

    @Serializable
    data class LinkAPIResponse(val link: String)
}
package io.github.jamalam360.api

import io.github.jamalam360.util.client
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.serialization.Serializable

/**
 * @author  Jamalam360
 */
class HastebinApi {
    suspend fun pasteFromCdn(url: String, cdnUrl: String): String {
        return paste(
            url, String(client.get<HttpResponse>(cdnUrl).content.toInputStream()
                .readAllBytes())
        )
    }

    suspend fun paste(url: String, text: String): String {
        client.post<HastebinApiResponse>(url + "documents") {
            body = text
        }.let {
            return it.key
        }
    }

    @Serializable
    data class HastebinApiResponse(val key: String)
}
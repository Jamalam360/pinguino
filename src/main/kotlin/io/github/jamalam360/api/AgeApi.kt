package io.github.jamalam360.api

import io.github.jamalam360.util.lenientClient
import io.ktor.client.request.*
import kotlinx.serialization.Serializable
import java.net.URLEncoder
import java.nio.charset.Charset

/**
 * @author  Jamalam360
 */
class AgeApi {
    suspend fun predictAge(name: String): Int {
        return lenientClient.get<AgeApiResponse>(
            "https://api.agify.io/?name=${
                URLEncoder.encode(
                    name,
                    Charset.defaultCharset()
                )
            }"
        ).age
    }

    @Serializable
    data class AgeApiResponse(val name: String, val age: Int, val count: Int)
}
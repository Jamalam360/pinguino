package io.github.jamalam360.api

import io.github.jamalam360.util.DBL_TOKEN
import io.github.jamalam360.util.DBL_URL
import io.github.jamalam360.util.client
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @author  Jamalam360
 */
class TopGg {
    suspend fun sendServerCount(count: Int) {
        client.post<HttpResponse>(DBL_URL) {
            contentType(ContentType.Application.Json)
            header("Authorization", DBL_TOKEN)
            body = DBLStatisticBody(count)
        }
    }

    @Serializable
    data class DBLStatisticBody(
        @SerialName("server_count")
        val count: Int
    )
}
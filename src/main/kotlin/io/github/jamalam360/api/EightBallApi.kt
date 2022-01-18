package io.github.jamalam360.api

import io.github.jamalam360.util.lenientClient
import io.ktor.client.request.*
import kotlinx.serialization.Serializable
import java.net.URLEncoder
import java.nio.charset.Charset

/**
 * @author  Jamalam360
 */
class EightBallApi {
    suspend fun ask(question: String): String {
        return lenientClient.get<EightBallApiResponse>(
            "https://8ball.delegator.com/magic/JSON/" + URLEncoder.encode(
                question,
                Charset.defaultCharset()
            )
        ).magic.answer
    }

    @Serializable
    data class EightBallApiResponse(val magic: EightBall)

    @Serializable
    data class EightBall(val question: String, val answer: String, val type: String)
}
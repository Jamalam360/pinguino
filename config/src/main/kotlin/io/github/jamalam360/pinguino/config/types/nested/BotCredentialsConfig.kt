package io.github.jamalam360.pinguino.config.types.nested

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @author  Jamalam
 */

@Serializable
data class BotCredentialsConfig(
    @SerialName("discord_token")
    val discordToken: String,
)

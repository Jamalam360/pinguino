package io.github.jamalam360.pinguino.config.types.nested

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

/**
 * @author  Jamalam
 */

@Serializable
data class BotAdministrationConfig(
    val admins: List<ULong>,
    @SerialName("server_id")
    val serverId: ULong
)

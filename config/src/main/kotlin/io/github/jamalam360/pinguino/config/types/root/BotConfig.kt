package io.github.jamalam360.pinguino.config.types.root

import io.github.jamalam360.pinguino.config.types.nested.BotAdministrationConfig
import io.github.jamalam360.pinguino.config.types.nested.BotCredentialsConfig
import kotlinx.serialization.Serializable

/**
 * @author  Jamalam
 */

@Serializable
data class BotConfig(
    val credentials: BotCredentialsConfig,
    val administration: BotAdministrationConfig
)

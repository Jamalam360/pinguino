package io.github.jamalam360.pinguino.config.types.root

import io.github.jamalam360.pinguino.config.types.nested.DatabaseConfig
import kotlinx.serialization.Serializable

/**
 * @author  Jamalam
 */

@Serializable
data class CommonConfig(
    val database: DatabaseConfig
)

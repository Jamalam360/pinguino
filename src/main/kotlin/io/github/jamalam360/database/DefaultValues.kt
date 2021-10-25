@file:Suppress("unused")

package io.github.jamalam360.database

import dev.kord.common.entity.Snowflake
import io.github.jamalam360.database.entity.ServerConfig
import io.github.jamalam360.database.entity.ServerLoggingConfig
import io.github.jamalam360.database.entity.ServerModerationConfig
import io.github.jamalam360.database.entity.ServerQuotesConfig
import kotlin.reflect.KClass

/**
 * @author  Jamalam360
 */

fun KClass<ServerConfig>.getDefault(id: Snowflake): ServerConfig {
    return ServerConfig(
        id.value,
        ServerQuotesConfig::class.getDefault(),
        ServerLoggingConfig::class.getDefault(),
        ServerModerationConfig::class.getDefault()
    )
}

fun KClass<ServerQuotesConfig>.getDefault(): ServerQuotesConfig {
    return ServerQuotesConfig(
        true,
        null,
        true
    )
}

fun KClass<ServerLoggingConfig>.getDefault(): ServerLoggingConfig {
    return ServerLoggingConfig(
        true,
        null
    )
}

fun KClass<ServerModerationConfig>.getDefault(): ServerModerationConfig {
    return ServerModerationConfig(
        enabled = true,
        logActions = true,
        moderatorRole = 0,

        mutableListOf()
    )
}

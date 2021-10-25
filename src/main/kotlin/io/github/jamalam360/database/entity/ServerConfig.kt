package io.github.jamalam360.database.entity

/**
 * @author  Jamalam360
 */
data class ServerConfig(
    var id: Long,
    var quotesConfig: ServerQuotesConfig,
    var loggingConfig: ServerLoggingConfig,
    var moderationConfig: ServerModerationConfig
)

data class ServerQuotesConfig(
    var enabled: Boolean,
    var channel: Long?,
    var log: Boolean
)

data class ServerLoggingConfig(
    var enabled: Boolean,
    var channel: Long?
)

data class ServerModerationConfig(
    var enabled: Boolean,
    var logActions: Boolean,
    var moderatorRole: Long,
    var threadAutoJoinRoles: MutableList<Long>
)

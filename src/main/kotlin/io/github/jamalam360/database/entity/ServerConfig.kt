package io.github.jamalam360.database.entity

/**
 * @author  Jamalam360
 */

data class ServerConfig(
    var id: Long,
    var quotesConfig: ServerQuotesConfig,
    var loggingConfig: ServerLoggingConfig,
    var moderationConfig: ServerModerationConfig,
    var tagsConfig: ServerTagsConfig
)

data class ServerQuotesConfig(
    var enabled: Boolean,
    var channel: Long?
)

data class ServerLoggingConfig(
    var enabled: Boolean,
    var channel: Long?
)

data class ServerModerationConfig(
    var enabled: Boolean,
    var moderatorRole: Long,
    var mutedRole: Long,
    var threadAutoJoinRoles: MutableList<Long>
)

data class ServerTagsConfig(
    var tags: MutableMap<String, String>
)

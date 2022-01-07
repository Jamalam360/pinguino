package io.github.jamalam360.database.entity

/**
 * @author  Jamalam360
 */

data class ServerConfig(
    var id: Long,
    var quotesConfig: ServerQuotesConfig,
    var loggingConfig: ServerLoggingConfig,
    var moderationConfig: ServerModerationConfig,
    var tagsConfig: ServerTagsConfig,
    var notificationsConfig: ServerNotificationsConfig,
    var filePasteConfig: ServerFilePasteConfig
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

data class ServerNotificationsConfig(
    var enabled: Boolean,
    var greetingChannel: Long?,
    var greetingMessage: String?,
    var farewellMessage: String?
)

data class ServerFilePasteConfig(
    var enabled: Boolean,
    var hastebinUrl: String
)
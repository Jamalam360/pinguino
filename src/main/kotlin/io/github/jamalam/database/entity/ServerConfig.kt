/*
 * Copyright (C) 2022 Jamalam
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.jamalam.database.entity

import com.kotlindiscord.kord.extensions.commands.application.slash.converters.ChoiceEnum
import kotlinx.serialization.Serializable

/**
 * @author  Jamalam360
 */

@Serializable
data class ServerConfig(
    var id: Long,
    var quotesConfig: ServerQuotesConfig,
    var loggingConfig: ServerLoggingConfig,
    var moderationConfig: ServerModerationConfig,
    var tagsConfig: ServerTagsConfig,
    var notificationsConfig: ServerNotificationsConfig,
    var filePasteConfig: ServerFilePasteConfig,
    var phishingConfig: ServerPhishingConfig,
)

@Serializable
data class ServerQuotesConfig(
    var enabled: Boolean,
    var channel: Long?
)

@Serializable
data class ServerLoggingConfig(
    var enabled: Boolean,
    var channel: Long?
)

@Serializable
data class ServerModerationConfig(
    var enabled: Boolean,
    var moderatorRole: Long,
    var threadAutoJoinRoles: MutableList<Long>,
    var autoSaveThreads: Boolean,
    var publicModLogChannel: Long?
)

@Serializable
data class ServerTagsConfig(
    var enabled: Boolean,
    var tags: MutableMap<String, String>
)

@Serializable
data class ServerNotificationsConfig(
    var enabled: Boolean,
    var greetingChannel: Long?,
    var greetingMessage: String?,
    var farewellMessage: String?
)

@Serializable
data class ServerFilePasteConfig(
    var enabled: Boolean,
    var hastebinUrl: String
)

@Serializable
data class ServerPhishingConfig(
    var enabled: Boolean,
    var moderatorsExempt: Boolean,
    var moderationType: ServerPhishingModerationType,
)

enum class ServerPhishingModerationType : ChoiceEnum {
    Delete {
        override val readableName = "Delete"
    },
    Kick {
        override val readableName = "kick"
    },
    Ban {
        override val readableName = "ban"
    };
}

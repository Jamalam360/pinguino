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

@file:Suppress("unused")

package io.github.jamalam.database

import dev.kord.common.entity.Snowflake
import io.github.jamalam.database.entity.*
import kotlin.reflect.KClass

/**
 * @author  Jamalam360
 */

fun KClass<ServerConfig>.getDefault(id: Snowflake): ServerConfig {
    return ServerConfig(
        id.value.toLong(),
        ServerQuotesConfig::class.getDefault(),
        ServerLoggingConfig::class.getDefault(),
        ServerModerationConfig::class.getDefault(),
        ServerTagsConfig::class.getDefault(),
        ServerNotificationsConfig::class.getDefault(),
        ServerFilePasteConfig::class.getDefault(),
        ServerPhishingConfig::class.getDefault(),
        ServerRoleConfig::class.getDefault(),
    )
}

fun KClass<ServerQuotesConfig>.getDefault(): ServerQuotesConfig {
    return ServerQuotesConfig(
        true,
        null,
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
        moderatorRole = 0,
        mutableListOf(),
        autoSaveThreads = false,
        publicModLogChannel = 0
    )
}

fun KClass<ServerTagsConfig>.getDefault(): ServerTagsConfig {
    return ServerTagsConfig(
        true,
        HashMap()
    )
}

fun KClass<ServerNotificationsConfig>.getDefault(): ServerNotificationsConfig {
    return ServerNotificationsConfig(
        enabled = false,
        greetingChannel = null,
        greetingMessage = null,
        farewellMessage = null
    )
}

fun KClass<ServerFilePasteConfig>.getDefault(): ServerFilePasteConfig {
    return ServerFilePasteConfig(
        enabled = false,
        hastebinUrl = "https://www.toptal.com/developers/hastebin/"
    )
}

fun KClass<ServerPhishingConfig>.getDefault(): ServerPhishingConfig {
    return ServerPhishingConfig(
        enabled = false,
        moderatorsExempt = true,
        moderationType = ServerPhishingModerationType.Delete,
    )
}

fun KClass<ServerRoleConfig>.getDefault(): ServerRoleConfig {
    return ServerRoleConfig(
        enabled = false,
        roles = mutableMapOf(),
    )
}

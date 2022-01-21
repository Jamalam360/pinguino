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

package io.github.jamalam360.extensions.moderation

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.ChannelType
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.entity.Guild
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.event.guild.MemberJoinEvent
import dev.kord.core.event.guild.MemberLeaveEvent
import io.github.jamalam360.Modules
import io.github.jamalam360.util.database
import io.github.jamalam360.util.isModuleEnabled

/**
 * @author  Jamalam360
 */

@OptIn(KordPreview::class)
class NotificationsExtension : Extension() {
    override val name = "notifications"

    override suspend fun setup() {
        //region Events
        event<MemberJoinEvent> {
            check {
                isModuleEnabled(Modules.Notifications)
            }

            action {
                sendGreeting(event.guild.asGuild(), event.member.asUser())
            }
        }

        event<MemberLeaveEvent> {
            check {
                isModuleEnabled(Modules.Notifications)
            }

            action {
                sendFarewell(event.guild.asGuild(), event.user)
            }
        }
        //endregion
    }

    //region Util Methods
    private suspend fun sendGreeting(guild: Guild, user: User) {
        val conf = database.config.getConfig(guild.id)

        if (conf.notificationsConfig.greetingChannel != null) {
            val channel = guild.getChannel(Snowflake(conf.notificationsConfig.greetingChannel!!))

            if (channel.type == ChannelType.GuildText) {
                (channel as MessageChannel).createEmbed {
                    title = if (conf.notificationsConfig.greetingMessage == null) {
                        "Everybody welcome ${user.username} to ${guild.name}!"
                    } else {
                        conf.notificationsConfig.greetingMessage!!.replace("\$user", user.username)
                    }
                    image = user.avatar!!.url
                }
            }
        }
    }

    private suspend fun sendFarewell(guild: Guild, user: User) {
        val conf = database.config.getConfig(guild.id)

        if (conf.notificationsConfig.greetingChannel != null) {
            val channel = guild.getChannel(Snowflake(conf.notificationsConfig.greetingChannel!!))

            if (channel.type == ChannelType.GuildText) {
                (channel as MessageChannel).createEmbed {
                    title = if (conf.notificationsConfig.farewellMessage == null) {
                        "Everybody say goodbye to ${user.username}"
                    } else {
                        conf.notificationsConfig.farewellMessage!!.replace("\$user", user.username)
                    }
                    image = user.avatar!!.url
                }
            }
        }
    }
    //endregion
}

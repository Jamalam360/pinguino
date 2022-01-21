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

import com.kotlindiscord.kord.extensions.DISCORD_BLURPLE
import com.kotlindiscord.kord.extensions.checks.isNotBot
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import dev.kord.common.Color
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.ChannelType
import dev.kord.common.entity.MessageType
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.event.guild.MemberJoinEvent
import dev.kord.core.event.guild.MemberLeaveEvent
import dev.kord.core.event.message.MessageDeleteEvent
import dev.kord.core.event.message.MessageUpdateEvent
import io.github.jamalam360.util.*

/**
 * @author  Jamalam360
 */

@OptIn(KordPreview::class)
class LoggingExtension : Extension() {
    override val name: String = "logging"

    override suspend fun setup() {
        //region Events
        event<MemberJoinEvent> {
            action {
                event.guild.getLogChannel()?.createEmbed {
                    info("Member Joined")
                    userAuthor(event.member)
                    log()
                    now()
                    image(event.member.avatar?.url)
                }
            }
        }

        event<MemberLeaveEvent> {
            action {
                event.guild.getLogChannel()?.createEmbed {
                    info("Member Left")
                    userAuthor(event.user)
                    log()
                    now()
                    image(event.user.avatar?.url)
                }
            }
        }

        event<MessageDeleteEvent> {
            check {
                isNotBot()
            }

            action {
                event.guild!!.getLogChannel()?.createEmbed {
                    info("Message Deleted")
                    userAuthor(event.message!!.author!!)
                    log()
                    now()

                    if (event.message!!.content.isNotBlank()) {
                        stringField("Content", event.message!!.content)
                    }

                    if (event.message!!.attachments.isNotEmpty()) {
                        stringField("Attachments", event.message!!.attachments.joinToString("\n") { it.url })
                    }
                }
            }
        }

        event<MessageUpdateEvent> {
            check {
                isNotBot()
            }

            action {
                val msg = event.message.asMessage()
                @Suppress("SENSELESS_COMPARISON")
                if (msg != null && msg.type != MessageType.ChannelPinnedMessage && msg.content != null && msg.author != null && msg.content != event.old!!.content && msg.embeds.size == event.old!!.embeds.size) {
                    val before: String = if (event.old == null) {
                        "**Failed to fetch previous message. Pinguino may have been offline when it was sent.**"
                    } else {
                        event.old!!.content
                    }

                    msg.getGuild().getLogChannel()?.createEmbed {
                        info("Message Edited")
                        userAuthor(msg.author!!)
                        log()
                        now()
                        stringField("Before", before)
                        stringField("After", msg.content)
                    }
                }
            }
        }
        //endregion
    }

    @Deprecated("Use getLogChannel() instead.")
    suspend fun logAction(
        action: String,
        extraContent: String?,
        user: User,
        guild: Guild,
        colour: Color = DISCORD_BLURPLE,
        imageUrl: String = ""
    ): Message? {
        val conf = database.config.getConfig(guild.id)

        if (conf.loggingConfig.enabled && conf.loggingConfig.channel != null) {
            val channel = guild.getChannel(Snowflake(conf.loggingConfig.channel!!))

            if (channel.type == ChannelType.GuildText) {
                return (channel as MessageChannel).createEmbed {
                    title = action
                    description = extraContent
                    userAuthor(user)
                    color = colour
                    now()

                    if (image != "") {
                        image = imageUrl
                    }
                }
            }
        }

        return null
    }
}

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

package io.github.jamalam.extensions.moderation

import com.kotlindiscord.kord.extensions.DISCORD_BLURPLE
import com.kotlindiscord.kord.extensions.checks.isNotBot
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.utils.hasRoles
import com.kotlindiscord.kord.extensions.utils.isPublished
import com.kotlindiscord.kord.extensions.utils.translate
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
import dev.kord.core.event.guild.EmojisUpdateEvent
import dev.kord.core.event.guild.MemberJoinEvent
import dev.kord.core.event.guild.MemberLeaveEvent
import dev.kord.core.event.guild.MemberUpdateEvent
import dev.kord.core.event.message.MessageDeleteEvent
import dev.kord.core.event.message.MessageUpdateEvent
import dev.kord.core.event.message.ReactionRemoveEvent
import dev.kord.core.event.role.RoleCreateEvent
import dev.kord.core.event.role.RoleDeleteEvent
import dev.kord.core.event.role.RoleUpdateEvent
import io.github.jamalam.Modules
import io.github.jamalam.util.*
import java.util.*

/**
 * @author  Jamalam360
 */

@OptIn(KordPreview::class)
class LoggingExtension : Extension() {
    override val name: String = "logging"

    override suspend fun setup() {
        event<MemberJoinEvent> {
            check {
                isModuleEnabled(Modules.Logging)
            }

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
            check {
                isModuleEnabled(Modules.Logging)
            }

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
                isModuleEnabled(Modules.Logging)
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
                isModuleEnabled(Modules.Logging)
            }

            action {
                val msg = event.message.asMessage()

                if (msg.isPublished != event.old?.isPublished) {
                    msg.getGuild().getLogChannel()?.createEmbed {
                        info("Message Published")
                        pinguino()
                        log()
                        now()
                        channelField("Channel", msg.channel.asChannel())
                        stringField("Content", msg.content)
                    }
                }

                if (msg.isPinned != event.old?.isPinned) {
                    msg.getGuild().getLogChannel()?.createEmbed {
                        info(if (msg.isPinned) "Message Pinned" else "Message Unpinned")
                        pinguino()
                        log()
                        now()
                        channelField("Channel", msg.channel.asChannel())
                        stringField("Content", msg.content)
                    }
                }

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
                        channelField("Channel", msg.channel.asChannel())
                        stringField("Before", before)
                        stringField("After", msg.content)
                    }
                }
            }
        }

        event<ReactionRemoveEvent> {
            check {
                isModuleEnabled(Modules.Logging)
            }

            action {
                event.guild?.getLogChannel()?.createEmbed {
                    info("Reaction Removed")
                    userAuthor(event.getUser())
                    log()
                    now()
                    message(event.message.asMessage(), true)
                    stringField("Emoji", event.emoji.mention)
                }
            }
        }

        event<MemberUpdateEvent> {
            check {
                isModuleEnabled(Modules.Logging)
            }

            action {
                if (event.old == null) {
                    return@action
                }

                if (event.old!!.nickname != event.member.nickname) {
                    event.guild.getLogChannel()?.createEmbed {
                        info("Member Nickname Updated")
                        userAuthor(event.member)
                        log()
                        now()
                        stringField("Before", event.old!!.username)
                        stringField("After", event.member.username)
                    }
                }

                if (event.old!!.avatar?.url != event.member.avatar?.url) {
                    event.guild.getLogChannel()?.createEmbed {
                        info("Member Avatar Updated")
                        userAuthor(event.member)
                        log()
                        now()

                        if (event.old?.avatar?.url != null) {
                            stringField("Before", event.old!!.avatar!!.url)
                        } else {
                            stringField("Before", "Failed to fetch")
                        }

                        if (event.member.avatar?.url != null) {
                            stringField("After", event.member.avatar!!.url)
                        } else {
                            stringField("After", "Failed to fetch")
                        }
                    }
                }

                if (!event.member.hasRoles(event.old!!.roleBehaviors) || !event.old!!.hasRoles(event.member.roleBehaviors)) {
                    event.guild.getLogChannel()?.createEmbed {
                        info("Member Roles Updated")
                        userAuthor(event.member)
                        log()
                        now()

                        if (event.old?.roleBehaviors?.isNotEmpty() == true) {
                            stringField("Before", event.old!!.roleBehaviors.joinToString(",\n") { it.mention })
                        } else {
                            stringField("Before", "Empty")
                        }

                        if (event.member.roleBehaviors.isNotEmpty()) {
                            stringField("After", event.member.roleBehaviors.joinToString(",\n") { it.mention })
                        } else {
                            stringField("After", "Empty")
                        }
                    }
                }
            }
        }

        event<RoleCreateEvent> {
            check {
                isModuleEnabled(Modules.Logging)
            }

            action {
                event.guild.getLogChannel()?.createEmbed {
                    info("Role Created")
                    pinguino()
                    log()
                    now()
                    stringField("Name", event.role.name)
                    stringField("Color", event.role.color.toString())
                    stringField(
                        "Permissions",
                        event.role.permissions.values.joinToString(",\n") { it.translate(Locale.ENGLISH) })
                }
            }
        }

        event<RoleUpdateEvent> {
            check {
                isModuleEnabled(Modules.Logging)
            }

            action {
                if (event.old == null) {
                    return@action
                }

                if (event.old!!.name != event.role.name) {
                    event.guild.getLogChannel()?.createEmbed {
                        info("Role Name Updated")
                        pinguino()
                        log()
                        now()
                        stringField("Before", event.old!!.name)
                        stringField("After", event.role.name)
                    }
                }

                if (event.old!!.color != event.role.color) {
                    event.guild.getLogChannel()?.createEmbed {
                        info("Role Color Updated")
                        pinguino()
                        log()
                        now()
                        stringField("Before", event.old!!.color.toString())
                        stringField("After", event.role.color.toString())
                    }
                }

                if (event.old!!.permissions != event.role.permissions) {
                    event.guild.getLogChannel()?.createEmbed {
                        info("Role Permissions Updated")
                        pinguino()
                        log()
                        now()
                        stringField(
                            "Before",
                            event.old!!.permissions.values.joinToString(",\n") { it.translate(Locale.ENGLISH) })
                        stringField(
                            "After",
                            event.role.permissions.values.joinToString(",\n") { it.translate(Locale.ENGLISH) })
                    }
                }
            }
        }

        event<RoleDeleteEvent> {
            check {
                isModuleEnabled(Modules.Logging)
            }

            action {
                event.guild.getLogChannel()?.createEmbed {
                    info("Role Deleted")
                    pinguino()
                    log()
                    now()
                    stringField("Name", event.role!!.name)
                    stringField("Color", event.role!!.color.toString())
                    stringField(
                        "Permissions",
                        event.role!!.permissions.values.joinToString(",\n") { it.translate(Locale.ENGLISH) })
                }
            }
        }

        event<EmojisUpdateEvent> {
            check {
                isModuleEnabled(Modules.Logging)
            }

            action {
                event.guild.getLogChannel()?.createEmbed {
                    info("Emojis Updated")
                    pinguino()
                    log()
                    now()
                    stringField("Emojis", event.emojis.joinToString(",\n") { it.name + " - " + it.mention })
                }
            }
        }
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
        val conf = database.serverConfig.getConfig(guild.id)

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

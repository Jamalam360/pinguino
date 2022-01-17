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
                logAction("Member Joined", "", event.member, event.guild.asGuild())
            }
        }

        event<MemberLeaveEvent> {
            action {
                logAction("Member Left", "", event.user, event.guild.asGuild())
            }
        }

        event<MessageDeleteEvent> {
            check {
                isNotBot()
            }

            action {
                event.guild!!.getLogChannel()?.createEmbed {
                    title = "Message Deleted"

                    setAuthor(event.message!!.author!!)
                    info()
                    now()

                    if (event.message!!.content.isNotBlank()) {
                        field {
                            name = "Content"
                            value = event.message!!.content
                        }
                    }

                    if (event.message!!.attachments.isNotEmpty()) {
                        field {
                            name = "Attachments"
                            value = event.message!!.attachments.joinToString("\n") { it.url }
                        }
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
                        "**Failed to fetch previous message. The bot may have been offline when it was sent.**"
                    } else {
                        event.old!!.content
                    }

                    msg.getGuild().getLogChannel()?.createEmbed {
                        title = "Message Edited"

                        setAuthor(msg.author!!)
                        info()
                        now()

                        field {
                            name = "Before"
                            value = before
                        }

                        field {
                            name = "After"
                            value = msg.content
                        }
                    }
                }
            }
        }
        //endregion
    }

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
                    setAuthor(user)
                    color = colour //Bri'ish spelling best
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

package io.github.jamalam360.extensions.moderation

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.ChannelType
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
import dev.kord.rest.builder.message.EmbedBuilder
import io.github.jamalam360.DATABASE

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
            action {
                logAction("Message Deleted", event.message!!.content, event.message!!.author!!, event.guild!!.asGuild())
            }
        }

        event<MessageUpdateEvent> {
            action {
                logAction("Message Edited", event.message.asMessage().content, event.message.asMessage().author!!, event.getMessage().getGuild())
            }
        }
        //endregion
    }

    suspend fun logAction(action: String, extraContent: String?, user: User, guild: Guild): Message? {
        val conf = DATABASE.config.getConfig(guild.id)

        if (conf.loggingConfig.channel != null) {
            val channel = guild.getChannel(Snowflake(conf.loggingConfig.channel!!))

            if (channel.type == ChannelType.GuildText) {
                val embedAuthor = EmbedBuilder.Author()
                embedAuthor.name = user.username
                embedAuthor.icon = user.avatar.url

                return (channel as MessageChannel).createEmbed {
                    title = action
                    description = extraContent
                    author = embedAuthor
                }
            }
        }

        return null
    }
}

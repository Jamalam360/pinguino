package io.github.jamalam360.extensions

import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.ChannelType
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.rest.builder.message.EmbedBuilder
import io.github.jamalam360.DATABASE

/**
 * @author  Jamalam360
 */

@OptIn(KordPreview::class)
class LoggingExtension : Extension() {
    override val name: String = "logging"

    @Suppress("EmptyFunctionBlock")
    override suspend fun setup() {
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

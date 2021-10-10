package io.github.jamalam360.extensions

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.channel
import com.kotlindiscord.kord.extensions.commands.converters.impl.message
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.commands.converters.impl.user
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.extensions.publicMessageCommand
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.ChannelType
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.entity.Guild
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.event.message.ReactionAddEvent
import dev.kord.rest.builder.message.EmbedBuilder
import io.github.jamalam360.DATABASE

/**
 * @author  Jamalam360
 */

@OptIn(KordPreview::class)
class QuoteExtension : Extension() {
    override val name = "quotes"
    private val quoteText: String = "quote"

    override suspend fun setup() {
        // region Slash commands

        publicSlashCommand(::QuoteChannelSetArgs) {
            name = "quote-channel-set"
            description = "Set the channel for quotes to be sent to"

            action {
                val conf = DATABASE.config.getConfig(guild!!.id)
                conf.quoteChannel = arguments.channel.id.value

                DATABASE.config.updateConfig(guild!!.id, conf)

                respond {
                    content = "Successfully update quotes channel to ${arguments.channel.mention}"
                }
            }
        }

        publicSlashCommand {
            name = quoteText
            description = "Record a quote!"

            publicSubCommand(::QuoteArgsMention) {
                name = "user"
                description = "Uses a user mention as the author"

                action {
                    val kord = this@QuoteExtension.kord

                    if (arguments.author.id == kord.selfId) {
                        respond {
                            content = "Cannot quote my own messages!"
                        }
                    } else {
                        sendQuote(
                            this.guild!!.asGuild(),
                            arguments.quote,
                            arguments.author.username,
                            arguments.author.avatar.url
                        )

                        respond {
                            content = "Quoted successfully"
                        }
                    }
                }
            }

            publicSubCommand(::QuoteArgsString) {
                name = "non-user"
                description = "Uses any person as the author"

                action {
                    sendQuote(this.guild!!.asGuild(), arguments.quote, arguments.author, null)
                }
            }

            /*publicSubCommand(::QuoteArgsMessage) {
                name = "message"
                description = "Uses a message ID or a message link"

                action {
                    respond {
                        content = "${arguments.message.content} - ${arguments.message.author!!.mention}"
                    }
                }
            }*/
        }

        //endregion

        //region Message commands

        publicMessageCommand {
            name = quoteText

            action {
                val kord = this@QuoteExtension.kord

                if (targetMessages.first().author == null || targetMessages.first().author!!.id == kord.selfId) {
                    respond {
                        content = "Cannot quote my own messages!"
                    }
                } else {
                    sendQuote(
                        this.guild!!.asGuild(),
                        targetMessages.first().content,
                        targetMessages.first().author!!.username,
                        targetMessages.first().author!!.avatar.url
                    )
                }
            }
        }

        //endregion

        //region Events

        event<ReactionAddEvent> {
            action {
                if (event.emoji.name == "\u2B50") {
                    val msg = event.channel.getMessage(event.messageId)

                    sendQuote(event.guild!!.asGuild(), msg.content, msg.author!!.username, msg.author!!.avatar.url)
                }
            }
        }

        //endregion
    }

    private suspend fun sendQuote(guild: Guild, quote: String, quoteAuthor: String, authorIcon: String?) {
        val config = DATABASE.config.getConfig(guild.id)

        if (config.quoteChannel != null) {
            val channel = guild.getChannel(Snowflake(config.quoteChannel!!))

            if (channel.type == ChannelType.GuildText) {
                val embedAuthor = EmbedBuilder.Author()
                embedAuthor.name = quoteAuthor

                if (authorIcon != null) {
                    embedAuthor.icon = authorIcon
                }

                (channel as MessageChannel).createEmbed {
                    title = quote
                    author = embedAuthor
                }
            }
        }
    }

    // region Arguments

    inner class QuoteChannelSetArgs : Arguments() {
        val channel by channel(
            "channel",
            "The channel to send quotes to"
        )
    }

    inner class QuoteArgsMention : Arguments() {
        val quote by string(
            quoteText,
            "The quote"
        )
        val author by user("author", description = "The author of the quote")
    }

    inner class QuoteArgsString : Arguments() {
        val quote by string(
            quoteText,
            "The quote"
        )
        val author by string(
            "author",
            "The author of the quote"
        )
    }

    inner class QuoteArgsMessage : Arguments() {
        val message by message(
            "message",
            "The link or ID of the message to quote"
        )
    }

    // endregion
}

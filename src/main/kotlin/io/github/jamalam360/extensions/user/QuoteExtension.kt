package io.github.jamalam360.extensions.user

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.commands.converters.impl.user
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralMessageCommand
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.ChannelType
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.entity.Guild
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.event.message.ReactionAddEvent
import dev.kord.rest.builder.message.EmbedBuilder
import io.github.jamalam360.DATABASE
import io.github.jamalam360.Modules
import io.github.jamalam360.extensions.moderation.LoggingExtension
import io.github.jamalam360.isModuleEnabled

/**
 * @author  Jamalam360
 */

@OptIn(KordPreview::class)
class QuoteExtension : Extension() {
    override val name = "quotes"

    private val quoteText: String = "quote"

    override suspend fun setup() {
        // region Slash commands
        ephemeralSlashCommand {
            name = quoteText
            description = "Record a quote!"

            ephemeralSubCommand(::QuoteArgsMention) {
                name = "user"
                description = "Uses a user mention as the author"

                check {
                    isModuleEnabled(Modules.Quotes)
                }

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
                            arguments.author.avatar.url,
                            user.asUser()
                        )

                        respond {
                            content = "Quoted successfully"
                        }
                    }
                }
            }

            ephemeralSubCommand(::QuoteArgsString) {
                name = "non-user"
                description = "Uses any person as the author"

                check {
                    isModuleEnabled(Modules.Quotes)
                }

                action {
                    sendQuote(this.guild!!.asGuild(), arguments.quote, arguments.author, null, user.asUser())

                    respond {
                        content = "Quoted successfully"
                    }
                }
            }
        }
        //endregion

        //region Message commands
        ephemeralMessageCommand {
            name = "Quote"

            check {
                isModuleEnabled(Modules.Quotes)
            }

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
                        targetMessages.first().author!!.avatar.url,
                        user.asUser()
                    )

                    respond {
                        content = "Quoted successfully"
                    }
                }
            }
        }

        //endregion

        //region Events

        // TODO: This is deprecated, for removal when discord adds message command support to mobile
        event<ReactionAddEvent> {
            check {
                isModuleEnabled(Modules.Quotes)
            }

            action {
                if (event.emoji.name == "\u2B50") {
                    val msg = event.channel.getMessage(event.messageId)

                    sendQuote(
                        event.guild!!.asGuild(),
                        msg.content,
                        msg.author!!.username,
                        msg.author!!.avatar.url,
                        event.user.asUser()
                    )
                }
            }
        }
        //endregion
    }

    //region Util Methods
    private suspend fun sendQuote(guild: Guild, quote: String, quoteAuthor: String, authorIcon: String?, quoter: User) {
        val conf = DATABASE.config.getConfig(guild.id)

        if (conf.quotesConfig.channel != null) {
            val channel = guild.getChannel(Snowflake(conf.quotesConfig.channel!!))

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

                (bot.extensions["logging"] as LoggingExtension).logAction(
                    "Quote Sent",
                    "$quote - $quoteAuthor",
                    quoter,
                    guild
                )
            }
        }
    }
    //endregion

    // region Arguments
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
    // endregion
}

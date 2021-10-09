package io.github.jamalam360.extensions

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.message
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.commands.converters.impl.user
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.extensions.publicMessageCommand
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.annotation.KordPreview
import dev.kord.core.event.message.ReactionAddEvent

/**
 * @author  Jamalam360
 */

@OptIn(KordPreview::class)
class QuoteExtension : Extension() {
    override val name = "quotes"
    private val quoteText: String = "quote"

    override suspend fun setup() {
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
                        respond {
                            content = "${arguments.quote} - ${arguments.author.mention}"
                        }
                    }
                }
            }

            publicSubCommand(::QuoteArgsString) {
                name = "non-user"
                description = "Uses any person as the author"

                action {
                    respond {
                        content = "${arguments.quote} - ${arguments.author}"
                    }
                }
            }

            publicSubCommand(::QuoteArgsMessage) {
                name = "message"
                description = "Uses a message ID or a message link"

                action {
                    respond {
                        content = "${arguments.message.content} - ${arguments.message.author!!.mention}"
                    }
                }
            }
        }

        publicMessageCommand {
            name = quoteText

            action {
                val kord = this@QuoteExtension.kord

                if (targetMessages.first().author == null || targetMessages.first().author!!.id == kord.selfId) {
                    respond {
                        content = "Cannot quote my own messages!"
                    }
                } else {
                    respond {
                        content = "${targetMessages.first().content} - ${targetMessages.first().author!!.mention}"
                    }
                }
            }
        }

        event<ReactionAddEvent> {
            action {
                if (event.emoji.name == "\u2B50") {
                    event.channel.createMessage(
                        "${event.channel.getMessage(event.messageId).content} -" +
                                " ${event.channel.getMessage(event.messageId).author!!.mention}"
                    )
                }
            }
        }
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
}

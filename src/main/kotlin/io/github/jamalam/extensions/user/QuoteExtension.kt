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

package io.github.jamalam.extensions.user

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
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.event.message.ReactionAddEvent
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.embed
import io.github.jamalam.Modules
import io.github.jamalam.api.DogApi
import io.github.jamalam.util.*

/**
 * @author  Jamalam360
 */

@OptIn(KordPreview::class)
class QuoteExtension : Extension() {
    override val name = "quotes"

    private val quoteText: String = "quote"
    private val dog = DogApi()

    override suspend fun setup() {
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
                    if (guild!!.getConfig().quotesConfig.channel != null) {
                        sendQuote(
                            this.guild!!.asGuild(),
                            arguments.quote,
                            arguments.author.username,
                            arguments.author.avatar!!.url,
                            user.asUser()
                        )

                        respond {
                            embed {
                                info("Quote recorded")
                                pinguino()
                                now()
                                success()
                            }
                        }
                    } else {
                        respond {
                            embed {
                                info("The Quotes channel has not been configured")
                                pinguino()
                                now()
                                error()
                            }
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
                    if (guild!!.getConfig().quotesConfig.channel != null) {
                        sendQuote(this.guild!!.asGuild(), arguments.quote, arguments.author, null, user.asUser())
                        respond {
                            embed {
                                info("Quote recorded")
                                pinguino()
                                now()
                                success()
                            }
                        }
                    } else {
                        respond {
                            embed {
                                info("The Quotes channel has not been configured")
                                pinguino()
                                now()
                                error()
                            }
                        }
                    }
                }
            }
        }

        ephemeralMessageCommand {
            name = "Quote"

            check {
                isModuleEnabled(Modules.Quotes)
            }

            action {
                if (guild!!.getConfig().quotesConfig.channel != null) {
                    targetMessages.first().quote(user.asUser())

                    respond {
                        embed {
                            info("Quote recorded")
                            pinguino()
                            now()
                            success()
                        }
                    }
                } else {
                    respond {
                        embed {
                            info("The Quotes channel has not been configured")
                            pinguino()
                            now()
                            error()
                        }
                    }
                }
            }
        }

        //TODO: This is deprecated, for removal when discord adds message command support to mobile
        event<ReactionAddEvent> {
            check {
                isModuleEnabled(Modules.Quotes)

                event.message.asMessage().reactions.filter { it.emoji.name == "⭐" }.let {
                    if (it.isNotEmpty() && it[0].count > 1) {
                        fail("That message has already been quoted")
                    }
                }
            }

            action {
                if (event.emoji.name == "⭐") {
                    val msg = event.channel.getMessage(event.messageId)
                    msg.quote(event.user.asUser())
                }
            }
        }
    }

    private suspend fun sendQuote(guild: Guild, quote: String, quoteAuthor: String, authorIcon: String?, quoter: User) {
        val conf = database.config.getConfig(guild.id)

        if (conf.quotesConfig.channel != null) {
            val channel = guild.getChannel(Snowflake(conf.quotesConfig.channel!!))

            if (channel.type == ChannelType.GuildText) {
                val embedAuthor = EmbedBuilder.Author()
                embedAuthor.name = quoteAuthor

                if (authorIcon != null) {
                    embedAuthor.icon = authorIcon
                } else {
                    embedAuthor.icon = dog.getRandomDog()
                }

                (channel as MessageChannel).createEmbed {
                    title = quote
                    author = embedAuthor
                }

                guild.getLogChannel()?.createEmbed {
                    info("Quote sent")
                    userAuthor(quoter.asUser())
                    now()
                    log()
                    stringField("Quote", quote)
                    stringField("Author", quoteAuthor)
                }
            }
        }
    }

    private suspend fun Message.quote(quoter: User) {
        val guild = getGuild()
        val conf = database.config.getConfig(guild.id)
        val author2ElectricBoogaloo = this.author!!

        if (conf.quotesConfig.channel != null) {
            val channel = guild.getChannel(Snowflake(conf.quotesConfig.channel!!))

            if (channel.type == ChannelType.GuildText) {
                (channel as MessageChannel).createEmbed {
                    if (content.isNotEmpty()) {
                        title = content
                    }

                    if (attachments.isNotEmpty()) {
                        image = attachments.first().url
                    }

                    author {
                        name = author2ElectricBoogaloo.username
                        icon = author2ElectricBoogaloo.avatar!!.url
                    }
                }

                guild.getLogChannel()?.createEmbed {
                    info("Quote sent")
                    userAuthor(quoter.asUser())
                    now()
                    log()
                    stringField("Quote", content)
                    userField("Author", author2ElectricBoogaloo)
                }
            }
        }
    }

    inner class QuoteArgsMention : Arguments() {
        val quote by string {
            name = quoteText
            description = "The quote"
        }
        val author by user {
            name = "author"
            description = "The author of the quote"
        }
    }

    inner class QuoteArgsString : Arguments() {
        val quote by string {
            name = quoteText
            description = "The quote"
        }
        val author by string {
            name = "author"
            description = "The author of the quote"
        }
    }
}

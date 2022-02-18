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

package io.github.jamalam.extensions.bot

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.group
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalChannel
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralMessageCommand
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.dm
import dev.kord.common.entity.ChannelType
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.supplier.EntitySupplyStrategy
import dev.kord.rest.builder.message.create.embed
import io.github.jamalam.config.config
import io.github.jamalam.util.*

class AnnouncementExtension : Extension() {
    override val name = "announcements"

    override suspend fun setup() {
        ephemeralSlashCommand {
            name = "announcements"
            description = "Subscribe to announcements regarding Pinguino development and updates"

            group("subscribe") {
                description = "Subscribe to updates"

                ephemeralSubCommand {
                    name = "dm"
                    description = "Subscribe to announcements via DMs"

                    action {
                        if (user.asUser().getDmChannelOrNull() == null) {
                            respond {
                                embed {
                                    info("You do not accept DMs from non-friends, so I cannot change your update status")
                                    pinguino()
                                    error()
                                    now()
                                }
                            }
                        } else {
                            user.asUser().dm {
                                embed {
                                    info("You have subscribed to bot updates in this channel")
                                    pinguino()
                                    success()
                                    now()
                                }
                            }

                            database.announcementSubscribers.addSubscriber(user.asUser().getDmChannel().id)

                            respond {
                                embed {
                                    info("Successfully added you to the subscribers list")
                                    pinguino()
                                    success()
                                    now()
                                }
                            }
                        }
                    }
                }

                ephemeralSubCommand(::SubscribeChannelArgs) {
                    name = "channel"
                    description = "Subscribe to announcements via a channel"

                    check {
                        hasModeratorRole()
                    }

                    action {
                        val channel =
                            (arguments.channel?.withStrategy(EntitySupplyStrategy.cacheWithCachingRestFallback)
                                ?: channel.asChannel()) as GuildMessageChannel

                        guild?.getLogChannel()?.createEmbed {
                            info("Channel added to bot update list")
                            pinguino()
                            log()
                            now()
                            channelField("Channel", channel)
                        }

                        channel.createEmbed {
                            info("You have subscribed to bot updates in this channel")
                            pinguino()
                            success()
                            now()
                        }

                        database.announcementSubscribers.addSubscriber(channel.id)

                        respond {
                            embed {
                                info("Successfully added that channel to the subscribers list")
                                pinguino()
                                success()
                                now()
                            }
                        }
                    }
                }
            }

            group("unsubscribe") {
                description = "Unsubscribe to updates"

                ephemeralSubCommand {
                    name = "dm"
                    description = "Unsubscribe to announcements via DMs"

                    action {
                        if (user.asUser().getDmChannelOrNull() == null) {
                            respond {
                                embed {
                                    info("You do not accept DMs from non-friends, so I cannot change your update status")
                                    pinguino()
                                    error()
                                    now()
                                }
                            }
                        } else {
                            user.asUser().dm {
                                embed {
                                    info("You have unsubscribed from bot updates in this channel")
                                    pinguino()
                                    error()
                                    now()
                                }
                            }

                            database.announcementSubscribers.removeSubscriber(user.asUser().getDmChannel().id)

                            respond {
                                embed {
                                    info("Successfully removed you from the subscribers list")
                                    pinguino()
                                    success()
                                    now()
                                }
                            }
                        }
                    }
                }

                ephemeralSubCommand(::SubscribeChannelArgs) {
                    name = "channel"
                    description = "Unsubscribe to announcements via a channel"

                    check {
                        hasModeratorRole()
                    }

                    action {
                        val channel =
                            (arguments.channel?.withStrategy(EntitySupplyStrategy.cacheWithCachingRestFallback)
                                ?: channel.asChannel()) as GuildMessageChannel

                        guild?.getLogChannel()?.createEmbed {
                            info("Channel removed from bot update list")
                            pinguino()
                            log()
                            now()
                            channelField("Channel", channel)
                        }

                        channel.createEmbed {
                            info("You have unsubscribed from bot updates in this channel")
                            pinguino()
                            success()
                            now()
                        }

                        database.announcementSubscribers.removeSubscriber(channel.id)

                        respond {
                            embed {
                                info("Successfully removed that channel from the subscribers list")
                                pinguino()
                                success()
                                now()
                            }
                        }
                    }
                }
            }
        }

        ephemeralMessageCommand {
            name = "Announce"

            guild((if (config.production()) config.production!!.adminServerId else config.development!!.serverId)!!)

            check {
                allowUser((if (config.production()) config.production!!.adminId else config.development!!.adminId)!!)
            }

            action {
                database.announcementSubscribers.getSubscribers().forEach { subscriberId ->
                    val channel = this@AnnouncementExtension.kord.getChannel(subscriberId)

                    if (channel is MessageChannel) {
                        targetMessages.first().forward(channel)
                    }
                }
            }
        }
    }

    inner class SubscribeChannelArgs : Arguments() {
        val channel by optionalChannel {
            name = "channel"
            description = "The channel to send the updates to, or the current one if unspecified"
            requireChannelType(ChannelType.GuildText)
        }
    }
}

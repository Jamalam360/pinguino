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

package io.github.jamalam360.extensions.user

import com.kotlindiscord.kord.extensions.DISCORD_GREEN
import com.kotlindiscord.kord.extensions.checks.guildFor
import com.kotlindiscord.kord.extensions.checks.isInThread
import com.kotlindiscord.kord.extensions.checks.memberFor
import com.kotlindiscord.kord.extensions.checks.threadFor
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingBoolean
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalBoolean
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralMessageCommand
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.behavior.channel.threads.edit
import dev.kord.core.entity.channel.thread.ThreadChannel
import dev.kord.core.event.channel.thread.ThreadChannelCreateEvent
import dev.kord.core.event.channel.thread.ThreadUpdateEvent
import dev.kord.core.exception.EntityNotFoundException
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.embed
import io.github.jamalam360.api.HastebinApi
import io.github.jamalam360.api.LinkApi
import io.github.jamalam360.util.*
import kotlinx.coroutines.flow.toList

/**
 * @author  Jamalam360
 */
class UserUtilityExtension : Extension() {
    override val name: String = "user-utility"

    private val hasteBin = HastebinApi()
    private val link = LinkApi()

    override suspend fun setup() {
        event<ThreadChannelCreateEvent> {
            action {
                if (event.channel.guild.getConfig().moderationConfig.autoSaveThreads) {
                    event.channel.save(true)

                    event.channel.guild.getLogChannel()?.createEmbed {
                        title = "Thread Created"
                        description = "${event.channel.mention} saved automatically"
                        now()
                        info()
                        author {
                            name = "Pinguino"
                            icon = PINGUINO_PFP
                        }
                    }
                }
            }
        }

        event<ThreadUpdateEvent> {
            action {
                if (event.channel.isArchived && database.savedThreads.shouldSave(event.channel.id)) {
                    event.channel.edit {
                        archived = false
                        reason = "Preventing thread from being archived"
                    }
                }
            }
        }

        ephemeralSlashCommand {
            name = "invite"
            description = "Get an invite link for Pinguino!"

            action {
                val embed = EmbedBuilder()
                embed.image = PINGUINO_PFP
                embed.title = "Invite Pinguino!"
                embed.description = "Click [here]" +
                        "(https://discord.com/api/oauth2/authorize?client_id=896758540784500797&permissions=8&scope=bot%20applications.commands)" +
                        " to invite Pinguino to your own server"

                respond {
                    embeds.add(embed)
                }
            }
        }

        ephemeralSlashCommand {
            name = "thread"
            description = "Commands to manage threads"

            ephemeralSubCommand(::ThreadArchiveArgs) {
                name = "archive"
                description = "Archive the thread you are in, if you have permission"

                check {
                    isInThread()
                }

                action {
                    val channel = channel.asChannel() as ThreadChannel
                    val roles = user.asMember(guild!!.id).roles.toList()
                    val modRole = Snowflake(database.config.getConfig(guild!!.id).moderationConfig.moderatorRole)

                    if (roles.contains(guild!!.getRoleOrNull(modRole)) || channel.ownerId == user.id
                    ) {
                        if (!channel.isArchived) {
                            if (roles.contains(guild!!.getRoleOrNull(modRole)) && arguments.lock == true) {
                                channel.edit {
                                    locked = true
                                    this.archived = true
                                    reason = "Thread archived and locked by ${user.mention}"
                                }

                                respond {
                                    content = "Successfully archived and locked thread"
                                }
                            } else if (arguments.lock == true) {
                                channel.edit {
                                    this.archived = true
                                    reason = "Archived by ${user.mention}"
                                }

                                respond {
                                    content =
                                        "Successfully archived thread, but you do not have permission to lock this thread"
                                }
                            } else {
                                channel.edit {
                                    this.archived = true
                                    reason = "Archived by ${user.mention}"
                                }

                                respond {
                                    content = "Successfully archived thread"
                                }
                            }
                        } else {
                            respond {
                                content = "This thread is already archived"
                            }
                        }

                        bot.getLoggingExtension().logAction(
                            "Thread archived",
                            if (roles.contains(guild!!.getRoleOrNull(modRole)) && arguments.lock!!) "Locked" else "Not Locked",
                            user.asUser(),
                            guild!!.asGuild()
                        )
                    } else {
                        respond {
                            content = "You do not have permission to archive or lock this thread"
                        }
                    }
                }
            }

            ephemeralSubCommand(::ThreadRenameArgs) {
                name = "rename"
                description = "Rename the thread you are in, if you have permission"

                check {
                    isInThread()
                }

                action {
                    val channel = channel.asChannel() as ThreadChannel
                    val roles = user.asMember(guild!!.id).roles.toList()
                    val modRole = Snowflake(database.config.getConfig(guild!!.id).moderationConfig.moderatorRole)

                    if (roles.contains(guild!!.getRoleOrNull(modRole)) || channel.ownerId == user.id) {
                        val before = channel.name

                        channel.edit {
                            this.name = arguments.name
                            reason = "Renamed by ${user.mention}"
                        }

                        respond {
                            content = "Successfully renamed thread"
                        }

                        bot.getLoggingExtension().logAction(
                            "Thread renamed",
                            "'$before' --> '${arguments.name}",
                            user.asUser(),
                            guild!!.asGuild()
                        )
                    } else {
                        respond {
                            content = "You do not have permission to rename this thread"
                        }
                    }
                }
            }

            ephemeralSubCommand(::ThreadSaveArgs) {
                name = "save"
                description = "Prevent the thread you are in from archiving, if you have permission"

                check {
                    hasModeratorRole()
                    isInThread()
                }

                action {
                    (channel as ThreadChannel).save(arguments.save)

                    bot.getLoggingExtension().logAction(
                        if (arguments.save) "Thread Saved" else "Thread Unsaved",
                        channel.mention,
                        user.asUser(),
                        guild!!.asGuild()
                    )

                    respond {
                        content =
                            "Successfully ${if (arguments.save) "set thread to be saved" else "set thread to not be saved"}"
                    }
                }
            }
        }

        ephemeralSlashCommand {
            name = "help"
            description = "Get a link to the help page"

            action {
                val embed = EmbedBuilder()
                embed.image = PINGUINO_PFP
                embed.title = "Learn how to use Pinguino!"
                embed.description = "Click [here]" +
                        "(https://github.com/JamCoreDiscord/Pinguino/wiki)" +
                        " to learn about Pinguino's features and commands. " +
                        "If you have any issues or further questions, join the" +
                        " [support server](https://discord.gg/88PWg5TySd)"

                respond {
                    embeds.add(embed)
                }
            }
        }

        ephemeralSlashCommand {
            name = "bugs"
            description = "Get a link to the bug tracker"

            action {
                val embed = EmbedBuilder()
                embed.image = PINGUINO_PFP
                embed.title = "Report bugs with Pinguino"
                embed.description = "Click [here]" +
                        "(https://github.com/JamCoreDiscord/Pinguino/issues)" +
                        " to report bugs with Pinguino. Reports are appreciated " +
                        "and we will get to your report ASAP."

                respond {
                    embeds.add(embed)
                }
            }
        }

        ephemeralSlashCommand(::SingleLinkArgs) {
            name = "shorten-link"
            description = "Shorten a link"

            action {
                link.shorten(arguments.link).let {
                    respond {
                        embed {
                            title = "Shortened Link"
                            url = it
                            color = DISCORD_GREEN
                        }
                    }
                }
            }
        }

        ephemeralSlashCommand {
            name = "paste"
            description = "Upload a file to hastebin"

            //TODO: Better descriptions

            ephemeralSubCommand(::SingleLinkArgs) {
                name = "url"
                description = "Use a cdn.discordapp.com link to paste your file"

                action {
                    hasteBin.pasteFromCdn("https://www.toptal.com/developers/hastebin/documents", arguments.link).let {
                        respond {
                            embed {
                                title = "File Uploaded to Hastebin"
                                url =
                                    "https://www.toptal.com/developers/hastebin/${it}"
                                color = DISCORD_GREEN
                            }
                        }
                    }
                }
            }

            ephemeralSubCommand(::SingleStringArgs) {
                name = "typed"
                description = "Type out your file into the slash command arguments"

                action {
                    hasteBin.paste("https://www.toptal.com/developers/hastebin/documents", arguments.string).let {
                        respond {
                            embed {
                                title = "File Uploaded to Hastebin"
                                url =
                                    "https://www.toptal.com/developers/hastebin/${it}"
                                color = DISCORD_GREEN
                            }
                        }
                    }
                }
            }
        }

        ephemeralMessageCommand {
            name = "Pin In Thread"

            check {
                isInThread()
                failIf("You don't have permission to pin messages in this channel") {
                    val guild = guildFor(event)
                    val member = memberFor(event)
                    val thread = threadFor(event)

                    if (member == null || guild == null || thread == null) {
                        return@failIf false
                    }

                    try {
                        if (member.asMember().roles.toList()
                                .contains(guild.getRole(Snowflake(database.config.getConfig(guild.id).moderationConfig.moderatorRole)))
                        ) {
                            return@failIf true
                        }
                    } catch (e: EntityNotFoundException) {
                        if (member.asMember().getPermissions().contains(Permission.Administrator)) {
                            return@failIf true
                        }
                    }

                    if (thread.asChannel().ownerId == member.id) {
                        return@failIf true
                    }

                    return@failIf false
                }
            }

            action {
                targetMessages.first().pin("Pinned by ${user.asUser()}")

                respond {
                    content = "Message pinned!"
                }
            }
        }
    }

    inner class ThreadRenameArgs : Arguments() {
        val name by string(
            "name",
            "The threads new name"
        )
    }

    inner class ThreadSaveArgs : Arguments() {
        val save by defaultingBoolean(
            "save",
            "Whether or not to prevent the thread from archiving",
            true
        )
    }

    inner class ThreadArchiveArgs : Arguments() {
        val lock by optionalBoolean(
            "lock",
            "Whether to lock the thread as well, if you are a moderator"
        )
    }

    inner class SingleLinkArgs : Arguments() {
        val link by string(
            "link",
            "The link"
        )
    }
}
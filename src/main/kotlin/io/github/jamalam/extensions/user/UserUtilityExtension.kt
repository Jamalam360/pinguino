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

import com.kotlindiscord.kord.extensions.checks.guildFor
import com.kotlindiscord.kord.extensions.checks.isInThread
import com.kotlindiscord.kord.extensions.checks.memberFor
import com.kotlindiscord.kord.extensions.checks.threadFor
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingBoolean
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalBoolean
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.*
import com.kotlindiscord.kord.extensions.time.TimestampType
import com.kotlindiscord.kord.extensions.time.toDiscord
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.createdAt
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.behavior.channel.threads.edit
import dev.kord.core.behavior.reply
import dev.kord.core.entity.channel.thread.ThreadChannel
import dev.kord.core.event.channel.thread.ThreadChannelCreateEvent
import dev.kord.core.event.channel.thread.ThreadUpdateEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.exception.EntityNotFoundException
import dev.kord.rest.builder.message.create.embed
import io.github.jamalam.api.HastebinApi
import io.github.jamalam.util.*
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList

/**
 * @author  Jamalam360
 */
class UserUtilityExtension : Extension() {
    override val name: String = "user-utility"

    private val hasteBin = HastebinApi()

    override suspend fun setup() {
        event<MessageCreateEvent> {
            action {
                val kord = this@UserUtilityExtension.kord
                if (event.message.mentionedUserIds.contains(kord.selfId) && event.message.content.filterNot { it.isWhitespace() } == "<@${kord.selfId.value}>") {
                    event.message.reply {
                        content = "Hi, I'm Pinguino! To get started, use the `/help` command. Have fun!"
                    }
                }
            }
        }

        event<ThreadChannelCreateEvent> {
            action {
                if (event.channel.guild.getConfig().moderationConfig.autoSaveThreads) {
                    event.channel.save(true)

                    event.channel.guild.getLogChannel()?.createEmbed {
                        info("Thread saved")
                        pinguino()
                        log()
                        now()
                        channelField("Channel", event.channel)
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
                respond {
                    embed {
                        info("Invite Pinguino")
                        pinguino()
                        now()
                        success()
                        url =
                            "https://discord.com/api/oauth2/authorize?client_id=896758540784500797&permissions=8&scope=bot%20applications.commands"
                    }
                }
            }
        }

        ephemeralSlashCommand {
            name = "uptime"
            description = "Get my uptime"

            action {
                respond {
                    embed {
                        info("I have been awake for ${this@UserUtilityExtension.kord.getUptime().toPrettyString()}!")
                        pinguino()
                        success()
                        now()
                    }
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
                    val modRole = Snowflake(database.serverConfig.getConfig(guild!!.id).moderationConfig.moderatorRole)

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
                                    embed {
                                        info("Thread archived and locked")
                                        pinguino()
                                        now()
                                        success()
                                    }
                                }
                            } else if (arguments.lock == true) {
                                channel.edit {
                                    this.archived = true
                                    reason = "Archived by ${user.mention}"
                                }

                                respond {
                                    embed {
                                        info("Thread archived and not locked (lacking permission)")
                                        pinguino()
                                        now()
                                        success()
                                    }
                                }
                            } else {
                                channel.edit {
                                    this.archived = true
                                    reason = "Archived by ${user.mention}"
                                }

                                respond {
                                    embed {
                                        info("Thread archived")
                                        pinguino()
                                        now()
                                        success()
                                    }
                                }
                            }
                        } else {
                            respond {
                                embed {
                                    info("This thread is already archived")
                                    pinguino()
                                    now()
                                    error()
                                }
                            }

                            return@action
                        }

                        guild!!.getLogChannel()?.createEmbed {
                            info("Thread archived")
                            userAuthor(user.asUser())
                            now()
                            log()
                            stringField(
                                "Locked",
                                if (roles.contains(guild!!.getRoleOrNull(modRole)) && arguments.lock!!) "Yes" else "No"
                            )
                        }
                    } else {
                        respond {
                            embed {
                                info("You do not have permission to archive this thread")
                                pinguino()
                                now()
                                error()
                            }
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
                    val modRole = Snowflake(database.serverConfig.getConfig(guild!!.id).moderationConfig.moderatorRole)

                    if (roles.contains(guild!!.getRoleOrNull(modRole)) || channel.ownerId == user.id) {
                        val before = channel.name

                        channel.edit {
                            this.name = arguments.name
                            reason = "Renamed by ${user.mention}"
                        }

                        guild!!.getLogChannel()?.createEmbed {
                            info("Thread renamed")
                            userAuthor(user.asUser())
                            now()
                            log()
                            stringField("Before", before)
                            stringField("After", arguments.name)
                        }

                        respond {
                            embed {
                                info("Thread renamed")
                                pinguino()
                                now()
                                success()
                            }
                        }
                    } else {
                        respond {
                            embed {
                                info("You do not have permission to rename this thread")
                                pinguino()
                                now()
                                error()
                            }
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

                    guild!!.getLogChannel()?.createEmbed {
                        info("Thread save status updated")
                        userAuthor(user.asUser())
                        now()
                        log()
                        stringField("Saved", if (arguments.save) "Yes" else "No")
                    }

                    respond {
                        embed {
                            info(if (arguments.save) "Thread saved" else "Thread unsaved")
                            pinguino()
                            now()
                            success()
                        }
                    }
                }
            }
        }

        ephemeralSlashCommand {
            name = "help"
            description = "Get a link to the help page"

            action {
                respond {
                    embed {
                        info("Help Pages")
                        pinguino()
                        now()
                        success()
                        stringField("Issue Tracker", "[Link](https://github.com/JamCoreDiscord/Pinguino/issues)")
                        stringField(
                            "Wiki",
                            "[Link](https://github.com/JamCoreDiscord/Pinguino/blob/release/docs/README.md)"
                        )
                        stringField("Support", "[Link](https://discord.jamalam.tech)")
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
                                info("File Uploaded to Hastebin")
                                pinguino()
                                now()
                                success()
                                url =
                                    "https://www.toptal.com/developers/hastebin/${it}"
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
                                info("File Uploaded to Hastebin")
                                pinguino()
                                now()
                                success()
                                url =
                                    "https://www.toptal.com/developers/hastebin/${it}"
                            }
                        }
                    }
                }
            }
        }

        ephemeralSlashCommand(::SingleUserArgs) {
            name = "user-info"
            description = "Get information about a user"

            action {
                respond {
                    embed {
                        info("Info for ${arguments.user.mention}")
                        pinguino()
                        now()
                        success()

                        stringField("Username", arguments.user.username)
                        stringField("Discriminator", "#${arguments.user.discriminator}")
                        stringField("ID", arguments.user.id.value.toString())
                        stringField("Created At", arguments.user.createdAt.toDiscord(TimestampType.Default))

                        if (guild != null) {
                            var roles = ""

                            arguments.user.asMember(guild!!.id).roles.map { it.mention }.collect {
                                roles += "$it,\n"
                            }

                            if (roles.isNotBlank()) {
                                stringField(
                                    "Roles",
                                    roles
                                )
                            } else {
                                stringField("Roles", "Empty")
                            }
                        }

                        image = arguments.user.avatar?.url
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
                                .contains(guild.getRole(Snowflake(database.serverConfig.getConfig(guild.id).moderationConfig.moderatorRole)))
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
                    embed {
                        info("Pinned Message")
                        pinguino()
                        now()
                        success()
                    }
                }
            }
        }

        ephemeralUserCommand {
            name = "Info"

            action {
                respond {
                    embed {
                        info("Info for ${targetUsers.first().username}#${targetUsers.first().discriminator}")
                        pinguino()
                        now()
                        success()

                        stringField("Username", targetUsers.first().username)

                        if (guild != null && targetUsers.first().asMember(guild!!.id).nickname != null) {
                            stringField("Nickname", targetUsers.first().asMember(guild!!.id).nickname!!)
                        }

                        stringField("Discriminator", "#${targetUsers.first().discriminator}")
                        stringField("ID", targetUsers.first().id.value.toString())
                        stringField(
                            "Created At",
                            "${targetUsers.first().createdAt.toDiscord(TimestampType.Default)} (${
                                targetUsers.first().createdAt.toDiscord(TimestampType.RelativeTime)
                            })"
                        )

                        if (guild != null) {
                            var roles = ""

                            targetUsers.first().asMember(guild!!.id).roles.map { it.mention }.collect {
                                roles += "$it,\n"
                            }

                            if (roles.isNotBlank()) {
                                stringField(
                                    "Roles",
                                    roles
                                )
                            } else {
                                stringField("Roles", "Empty")
                            }
                        }

                        image = targetUsers.first().avatar?.url
                    }
                }
            }
        }
    }

    inner class ThreadRenameArgs : Arguments() {
        val name by string {
            name = "name"
            description = "The threads new name"
        }
    }

    inner class ThreadSaveArgs : Arguments() {
        val save by defaultingBoolean {
            name = "save"
            description = "Whether or not to prevent the thread from archiving"
            defaultValue = true
        }
    }

    inner class ThreadArchiveArgs : Arguments() {
        val lock by optionalBoolean {
            name = "lock"
            description = "Whether to lock the thread as well, if you are a moderator"
        }
    }

    inner class SingleLinkArgs : Arguments() {
        val link by string {
            name = "link"
            description = "The link"
        }
    }
}
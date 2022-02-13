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

package io.github.jamalam.extensions.moderation

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.group
import com.kotlindiscord.kord.extensions.commands.converters.impl.*
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.dm
import com.kotlindiscord.kord.extensions.utils.timeoutUntil
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.ban
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.behavior.channel.editRolePermission
import dev.kord.core.behavior.channel.threads.edit
import dev.kord.core.behavior.channel.withTyping
import dev.kord.core.behavior.edit
import dev.kord.core.entity.channel.Channel
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.entity.channel.thread.TextChannelThread
import dev.kord.core.event.channel.thread.TextChannelThreadCreateEvent
import dev.kord.rest.builder.message.create.embed
import io.github.jamalam.Modules
import io.github.jamalam.util.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

/**
 * @author Jamalam360
 */

@Suppress("DuplicatedCode")
@OptIn(KordPreview::class, ExperimentalTime::class)
class ModerationExtension : Extension() {
    override val name: String = "moderation"

    private val speakingPermissions: Array<Permission> = arrayOf(
        Permission.SendMessages,
        Permission.AddReactions,
        Permission.CreatePublicThreads,
        Permission.CreatePrivateThreads,
        Permission.SendMessagesInThreads,
    )

    override suspend fun setup() {
        ephemeralSlashCommand {
            name = "moderation"
            description = "Commands for moderators"

            check {
                hasModeratorRole()
                isModuleEnabled(Modules.Moderation)
                notInDm()
            }

            group("thread-auto-join") {
                description = "Commands for the management of the thread auto-joiner feature"

                ephemeralSubCommand(::SingleRoleArgs) {
                    name = "add-role"
                    description = "Add a role to the thread auto-join list"

                    action {
                        val conf = database.serverConfig.getConfig(guild!!.id)

                        if (conf.moderationConfig.threadAutoJoinRoles.contains(arguments.role.id.value.toLong())) {
                            respond {
                                embed {
                                    info("That role is already on the auto-join list")
                                    pinguino()
                                    now()
                                    error()
                                }
                            }
                        } else {
                            conf.moderationConfig.threadAutoJoinRoles.add(arguments.role.id.value.toLong())
                            database.serverConfig.updateConfig(guild!!.id, conf)

                            guild?.getLogChannel()?.createEmbed {
                                info("Role added to thread auto-join list")
                                userAuthor(member!!.asUser())
                                now()
                                log()
                                stringField("Role", arguments.role.name)
                            }

                            respond {
                                embed {
                                    info("Added role to the auto-join list")
                                    pinguino()
                                    now()
                                    success()
                                }
                            }
                        }
                    }
                }

                ephemeralSubCommand(::SingleRoleArgs) {
                    name = "remove-role"
                    description = "Remove a role from the thread auto-join list"

                    action {
                        val conf = database.serverConfig.getConfig(guild!!.id)

                        if (conf.moderationConfig.threadAutoJoinRoles.contains(arguments.role.id.value.toLong())) {
                            conf.moderationConfig.threadAutoJoinRoles.remove(arguments.role.id.value.toLong())
                            database.serverConfig.updateConfig(guild!!.id, conf)

                            guild?.getLogChannel()?.createEmbed {
                                info("Role removed from the thread auto-join list")
                                userAuthor(member!!.asUser())
                                now()
                                log()
                                stringField("Role", arguments.role.name)
                            }

                            respond {
                                embed {
                                    info("Remove role from the auto-join list")
                                    pinguino()
                                    now()
                                    success()
                                }
                            }
                        } else {
                            respond {
                                embed {
                                    info("That role is not on the auto-join list")
                                    pinguino()
                                    now()
                                    success()
                                }
                            }
                        }
                    }
                }
            }

            group("discipline") {
                description = "Commands to discipline members"

                ephemeralSubCommand(::MuteArgs) {
                    name = "mute"
                    description = "Mute a member"

                    action {
                        val member = guild!!.getMemberOrNull(arguments.user.id)

                        if (member == null) {
                            respond {
                                embed {
                                    info("Cannot find that member")
                                    pinguino()
                                    now()
                                    error()
                                }
                            }
                        } else if (arguments.duration.seconds / 60 / 60 / 24 > 30) {
                            respond {
                                embed {
                                    info("Duration cannot be longer than 30 days")
                                    pinguino()
                                    now()
                                    error()
                                }
                            }
                        } else {
                            if (!member.isBot) {
                                member.dm {
                                    embed {
                                        info("Muted in ${guild!!.asGuild().name}!")
                                        userAuthor(user.asUser())
                                        now()
                                        error()
                                        stringField("Duration", arguments.duration.toPrettyString())
                                        stringField("Reason", arguments.reason)
                                    }
                                }
                            }

                            scheduler.schedule(arguments.duration.seconds.toLong()) {
                                if (!member.isBot) {
                                    member.dm {
                                        embed {
                                            info("Unmuted in ${guild!!.asGuild().name}!")
                                            userAuthor(user.asUser())
                                            now()
                                            success()
                                        }
                                    }
                                }

                                guild?.getLogChannel()?.createEmbed {
                                    info("Member Unmuted")
                                    userAuthor(user.asUser())
                                    log()
                                    userField("Member", member.asUser())
                                }

                                guild?.getPublicModLogChannel()?.createEmbed {
                                    info("Member Unmuted")
                                    userAuthor(user.asUser())
                                    log()
                                    userField("Member", member.asUser())
                                }
                            }

                            member.edit {
                                timeoutUntil =
                                    Clock.System.now().plus(arguments.duration, TimeZone.currentSystemDefault())

                                reason =
                                    "Muted by ${user.asUser().username} for ${arguments.duration} with the reason '${arguments.reason}'"
                            }

                            guild?.getLogChannel()?.createEmbed {
                                info("Member Muted")
                                userAuthor(user.asUser())
                                now()
                                log()
                                userField("Member", member.asUser())
                                stringField("Duration", arguments.duration.toPrettyString())
                                stringField("Reason", arguments.reason)
                            }

                            guild?.getPublicModLogChannel()?.createEmbed {
                                info("Member Muted")
                                userAuthor(user.asUser())
                                now()
                                log()
                                userField("Member", member.asUser())
                                stringField("Duration", arguments.duration.toPrettyString())
                                stringField("Reason", arguments.reason)
                            }

                            respond {
                                embed {
                                    info("Member Muted")
                                    pinguino()
                                    now()
                                    success()
                                }
                            }
                        }
                    }
                }

                ephemeralSubCommand(::SingleUserArgs) {
                    name = "unmute"
                    description = "Unmute a member"

                    action {
                        val member = guild!!.getMemberOrNull(arguments.user.id)

                        if (member == null) {
                            respond {
                                embed {
                                    info("Cannot find that member")
                                    pinguino()
                                    now()
                                    error()
                                }
                            }
                        } else {
                            if (!member.isBot) {
                                member.dm {
                                    embed {
                                        info("Unmuted in ${guild!!.asGuild().name}!")
                                        userAuthor(user.asUser())
                                        now()
                                        success()
                                    }
                                }
                            }

                            member.edit {
                                timeoutUntil = null
                                reason = "Unmuted by ${user.asUser().username}"
                            }

                            guild?.getLogChannel()?.createEmbed {
                                info("Member Unmuted")
                                userAuthor(user.asUser())
                                log()
                                userField("Member", member.asUser())
                            }

                            guild?.getPublicModLogChannel()?.createEmbed {
                                info("Member Unmuted")
                                userAuthor(user.asUser())
                                log()
                                userField("Member", member.asUser())
                            }

                            respond {
                                embed {
                                    info("Member Unmuted")
                                    pinguino()
                                    now()
                                    success()
                                }
                            }
                        }
                    }
                }

                ephemeralSubCommand(::KickArgs) {
                    name = "kick"
                    description = "Kick a member"

                    action {
                        val member = guild!!.getMemberOrNull(arguments.user.id)

                        if (member == null) {
                            respond {
                                embed {
                                    info("Cannot find that member")
                                    pinguino()
                                    now()
                                    error()
                                }
                            }
                        } else {
                            if (!member.isBot) {
                                member.dm {
                                    embed {
                                        info("Kicked from ${guild!!.asGuild().name}!")
                                        userAuthor(user.asUser())
                                        now()
                                        error()
                                        stringField("Reason", arguments.reason)
                                    }
                                }
                            }

                            member.kick("Kicked by ${user.asUser().username} with reason '${arguments.reason}'")

                            guild?.getLogChannel()?.createEmbed {
                                info("Member Kicked")
                                userAuthor(user.asUser())
                                now()
                                log()
                                userField("Member", member.asUser())
                                stringField("Reason", arguments.reason)
                            }

                            guild?.getPublicModLogChannel()?.createEmbed {
                                info("Member Kicked")
                                userAuthor(user.asUser())
                                now()
                                log()
                                userField("Member", member.asUser())
                                stringField("Reason", arguments.reason)
                            }

                            respond {
                                embed {
                                    info("Member Kicked")
                                    pinguino()
                                    now()
                                    success()
                                }
                            }
                        }
                    }
                }

                ephemeralSubCommand(::BanArgs) {
                    name = "ban"
                    description = "Ban a member"

                    action {
                        val member = guild!!.getMemberOrNull(arguments.user.id)

                        if (member == null) {
                            respond {
                                embed {
                                    info("Cannot find that member")
                                    pinguino()
                                    now()
                                    error()
                                }
                            }
                        } else {
                            if (!member.isBot) {
                                member.dm {
                                    embed {
                                        info("Banned from ${guild!!.asGuild().name}!")
                                        userAuthor(user.asUser())
                                        now()
                                        error()
                                        stringField("Reason", arguments.reason)
                                    }
                                }
                            }

                            member.ban {
                                deleteMessagesDays = if (arguments.deleteMessages) {
                                    7
                                } else {
                                    0
                                }
                                reason = "Banned by ${user.asUser().username} with reason '${arguments.reason}'"
                            }

                            guild?.getLogChannel()?.createEmbed {
                                info("Member Banned")
                                userAuthor(user.asUser())
                                now()
                                log()
                                userField("Member", member.asUser())
                                stringField("Reason", arguments.reason)
                            }

                            guild?.getPublicModLogChannel()?.createEmbed {
                                info("Member Banned")
                                userAuthor(user.asUser())
                                now()
                                log()
                                userField("Member", member.asUser())
                                stringField("Reason", arguments.reason)
                            }

                            respond {
                                embed {
                                    info("Member Banned")
                                    pinguino()
                                    now()
                                    success()
                                }
                            }
                        }
                    }
                }
            }

            ephemeralSubCommand(::LockArgs) {
                name = "lock"
                description = "Lock a channel"

                action {
                    val channel: Channel = if (arguments.channel != null) {
                        arguments.channel!!
                    } else {
                        channel.asChannel()
                    }

                    if (channel is TextChannelThread) {
                        channel.edit {
                            locked = true
                            reason = arguments.reason
                        }

                        channel.createEmbed {
                            info("Thread locked")
                            userAuthor(user.asUser())
                            now()
                            error()
                            stringField("Reason", arguments.reason)
                        }

                        guild?.getLogChannel()?.createEmbed {
                            info("Thread locked")
                            userAuthor(user.asUser())
                            now()
                            log()
                            channelField("Channel", channel.asChannel())
                            stringField("Reason", arguments.reason)
                        }

                        guild?.getPublicModLogChannel()?.createEmbed {
                            info("Thread locked")
                            userAuthor(user.asUser())
                            now()
                            log()
                            channelField("Channel", channel.asChannel())
                            stringField("Reason", arguments.reason)
                        }

                        if (arguments.duration != null) {
                            scheduler.schedule(arguments.duration!!.seconds.toLong()) {
                                channel.edit {
                                    locked = false
                                    reason = arguments.reason
                                }

                                channel.createEmbed {
                                    info("Thread unlocked")
                                    userAuthor(user.asUser())
                                    now()
                                    success()
                                }

                                guild?.getLogChannel()?.createEmbed {
                                    info("Thread unlocked automatically after timeout")
                                    userAuthor(user.asUser())
                                    now()
                                    log()
                                    channelField("Channel", channel.asChannel())
                                }
                            }
                        }
                    } else {
                        val text = channel as TextChannel

                        text.editRolePermission(guild!!.id) {
                            speakingPermissions.forEach {
                                denied += it
                            }

                            reason = arguments.reason
                        }

                        text.createMessage("Channel locked by a moderator")

                        channel.createEmbed {
                            info("Channel locked")
                            userAuthor(user.asUser())
                            now()
                            error()
                            stringField("Reason", arguments.reason)
                        }

                        guild?.getLogChannel()?.createEmbed {
                            info("Channel locked")
                            userAuthor(user.asUser())
                            now()
                            log()
                            channelField("Channel", channel.asChannel())
                            stringField("Reason", arguments.reason)
                        }

                        guild?.getPublicModLogChannel()?.createEmbed {
                            info("Channel locked")
                            userAuthor(user.asUser())
                            now()
                            log()
                            channelField("Channel", channel.asChannel())
                            stringField("Reason", arguments.reason)
                        }

                        if (arguments.duration != null) {
                            scheduler.schedule(arguments.duration!!.seconds.toLong()) {
                                text.editRolePermission(guild!!.id) {
                                    speakingPermissions.forEach {
                                        allowed += it
                                    }

                                    reason = arguments.reason
                                }

                                channel.createEmbed {
                                    info("Channel unlocked")
                                    userAuthor(user.asUser())
                                    now()
                                    success()
                                }

                                guild?.getLogChannel()?.createEmbed {
                                    info("Channel unlocked automatically after timeout")
                                    userAuthor(user.asUser())
                                    now()
                                    log()
                                    channelField("Channel", channel.asChannel())
                                }

                                guild?.getPublicModLogChannel()?.createEmbed {
                                    info("Channel unlocked")
                                    userAuthor(user.asUser())
                                    now()
                                    log()
                                    channelField("Channel", channel.asChannel())
                                }
                            }
                        }
                    }

                    respond {
                        embed {
                            info("Channel locked")
                            pinguino()
                            now()
                            success()
                        }
                    }
                }
            }

            ephemeralSubCommand(::UnlockArgs) {
                name = "unlock"
                description = "Unlock a channel"

                action {
                    val channel: Channel = if (arguments.channel != null) {
                        arguments.channel!!
                    } else {
                        channel.asChannel()
                    }

                    when (channel) {
                        is TextChannelThread -> {
                            channel.edit {
                                locked = true
                                reason = "Thread being unlocked by ${user.mention}"
                            }

                            channel.createEmbed {
                                info("Thread unlocked")
                                userAuthor(user.asUser())
                                now()
                                success()
                            }
                        }

                        is TextChannel -> {
                            channel.editRolePermission(guild!!.id) {
                                speakingPermissions.forEach {
                                    allowed += it
                                }

                                reason = "Channel being unlocked by ${user.asUser().username}"
                            }

                            channel.createEmbed {
                                info("Channel unlocked")
                                userAuthor(user.asUser())
                                now()
                                success()
                            }
                        }

                        else -> {
                            respond {
                                embed {
                                    info("Unsupported channel type")
                                    pinguino()
                                    now()
                                    error()
                                }
                            }

                            return@action
                        }
                    }

                    guild?.getLogChannel()?.createEmbed {
                        info("Channel unlocked")
                        userAuthor(user.asUser())
                        now()
                        log()
                        channelField("Channel", channel.asChannel())
                    }

                    guild?.getPublicModLogChannel()?.createEmbed {
                        info("Channel unlocked")
                        userAuthor(user.asUser())
                        now()
                        log()
                        channelField("Channel", channel.asChannel())
                    }

                    respond {
                        embed {
                            info("Channel unlocked")
                            pinguino()
                            now()
                            success()
                        }
                    }
                }
            }
        }

        event<TextChannelThreadCreateEvent> {
            check { failIf(event.channel.member != null) }

            action {
                val author = if (event.channel.owner.id == this@ModerationExtension.kord.selfId) {
                    event.channel.messages.first().author!!
                } else {
                    event.channel.owner
                }

                if (database.serverConfig.getConfig(event.channel.guildId).moderationConfig.threadAutoJoinRoles.isNotEmpty()) {
                    val msg =
                        event.channel.createMessage("Nice thread ${author.mention}! Hold on while I get some VIPs in here!")

                    event.channel.withTyping {
                        delay(Duration.Companion.seconds(6))
                    }

                    var mentions = ""
                    val roles =
                        database.serverConfig.getConfig(event.channel.guildId).moderationConfig.threadAutoJoinRoles

                    for (id in roles) {
                        mentions += "${event.channel.guild.getRole(Snowflake(id)).mention}, "
                    }

                    msg.edit {
                        content = "Hey ${mentions.dropLast(2)} come look at this cool thread!"
                    }

                    event.channel.withTyping {
                        delay(Duration.Companion.seconds(6))
                    }

                    msg.edit {
                        content = "Welcome to your thread ${author.mention}, enjoy!"
                    }
                } else {
                    event.channel.createMessage("Welcome to your thread ${author.mention}, enjoy!")
                }
            }
        }
    }

    inner class MuteArgs : SingleUserArgs() {
        val reason by string {
            name = "reason"
            description = "The reason for the mute"
        }
        val duration by duration {
            name = "duration"
            description = "The duration of the mute - optionally"
        }
    }

    inner class KickArgs : SingleUserArgs() {
        val reason by string {
            name = "reason"
            description = "The reason for the kick"
        }
    }

    inner class BanArgs : SingleUserArgs() {
        val reason by string {
            name = "reason"
            description = "The reason for the ban"
        }
        val deleteMessages by boolean {
            name = "delete-message-history"
            description = "Whether to delete the users message history"
        }
    }

    inner class LockArgs : Arguments() {
        val reason by string {
            name = "reason"
            description = "The reason for the lock"
        }
        val channel by optionalChannel {
            name = "channel"
            description = "The channel to lock, optionally"
        }
        val duration by optionalDuration {
            name = "duration"
            description = "The duration of the lock, optionally"
        }
    }

    inner class UnlockArgs : Arguments() {
        val channel by optionalChannel {
            name = "channel"
            description = "The channel to unlock, optionally"
        }
    }
}

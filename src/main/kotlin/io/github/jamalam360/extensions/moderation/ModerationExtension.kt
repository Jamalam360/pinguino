package io.github.jamalam360.extensions.moderation

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.group
import com.kotlindiscord.kord.extensions.commands.converters.impl.boolean
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalChannel
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalDuration
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.dm
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.ban
import dev.kord.core.behavior.channel.editRolePermission
import dev.kord.core.behavior.channel.threads.edit
import dev.kord.core.behavior.channel.withTyping
import dev.kord.core.behavior.edit
import dev.kord.core.entity.channel.Channel
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.entity.channel.thread.TextChannelThread
import dev.kord.core.event.channel.thread.TextChannelThreadCreateEvent
import dev.kord.rest.builder.message.EmbedBuilder
import io.github.jamalam360.Modules
import io.github.jamalam360.util.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

/**
 * @author Jamalam360
 */

@OptIn(KordPreview::class)
class ModerationExtension : Extension() {
    override val name: String = "moderation"

    private val speakingPermissions: Array<Permission> = arrayOf(
        Permission.SendMessages,
        Permission.AddReactions,
        Permission.CreatePublicThreads,
        Permission.CreatePrivateThreads,
        Permission.SendMessagesInThreads,
    )

    @OptIn(ExperimentalTime::class)
    override suspend fun setup() {
        //region Slash Commands
        ephemeralSlashCommand {
            name = "moderation"
            description = "Commands for moderators"

            check {
                hasModeratorRole()
                isModuleEnabled(Modules.Moderation)
            }

            group("thread-auto-join") {
                description = "Commands for the management of the thread auto-joiner feature"

                ephemeralSubCommand(::SingleRoleArgs) {
                    name = "add-role"
                    description = "Add a role to the thread auto-join list"

                    action {
                        val conf = database.config.getConfig(guild!!.id)

                        if (conf.moderationConfig.threadAutoJoinRoles.contains(arguments.role.id.value.toLong())) {
                            respond {
                                content = "${arguments.role.mention} is already on the auto-join list!"
                            }
                        } else {
                            conf.moderationConfig.threadAutoJoinRoles.add(arguments.role.id.value.toLong())
                            database.config.updateConfig(guild!!.id, conf)

                            respond {
                                content = "Successfully added ${arguments.role.mention} to the auto-join list"
                            }
                        }
                    }
                }

                ephemeralSubCommand(::SingleRoleArgs) {
                    name = "remove-role"
                    description = "Remove a role from the thread auto-join list"

                    action {
                        val conf = database.config.getConfig(guild!!.id)

                        if (conf.moderationConfig.threadAutoJoinRoles.contains(arguments.role.id.value.toLong())) {
                            conf.moderationConfig.threadAutoJoinRoles.remove(arguments.role.id.value.toLong())
                            database.config.updateConfig(guild!!.id, conf)

                            respond {
                                content = "Successfully removed ${arguments.role.mention} from the auto-join list"
                            }
                        } else {
                            respond {
                                content = "${arguments.role.mention} is not on the auto-join list"
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
                        val role = database.config.getConfig(guild!!.id).moderationConfig.mutedRole

                        if (guild!!.asGuild().getRoleOrNull(Snowflake(role)) == null) {
                            respond {
                                content = "The muted role is not yet configured for this server"
                            }

                            return@action
                        }

                        if (member == null) {
                            respond {
                                content = "Cannot find that member!"
                            }
                        } else {
                            if (!member.isBot) {
                                member.dm {
                                    val embed = EmbedBuilder()
                                    embed.title = "Muted in ${guild!!.asGuild().name}!"
                                    embed.description =
                                        "You have been muted in ${guild!!.asGuild().name} for ${arguments.duration.toString()} with the reason '${arguments.reason}'"
                                    embed.footer = EmbedBuilder.Footer()
                                    embed.footer!!.text = "Responsible moderator: ${user.asUser().username}"

                                    embeds.add(embed)
                                }
                            }

                            member.addRole(
                                Snowflake(role),
                                "Muted by ${user.asUser().username} for ${arguments.duration.toString()} with the reason '${arguments.reason}'"
                            )

                            if (arguments.duration != null) {
                                scheduler.schedule(arguments.duration!!.seconds.toLong()) {
                                    member.removeRole(
                                        Snowflake(role),
                                        "Automatic unmute from mute made ${arguments.duration} ago"
                                    )
                                }

                                bot.getLoggingExtension().logAction(
                                    "Member Unmuted",
                                    "Automatic unmute from mute made ${arguments.duration} ago",
                                    member.asUser(),
                                    guild!!.asGuild()
                                )
                                if (!member.isBot) {
                                    member.dm {
                                        val embed = EmbedBuilder()
                                        embed.title = "Unmuted in ${guild!!.asGuild().name}!"
                                        embed.description =
                                            "You have been automatically unmuted from a mute made ${arguments.duration} ago"
                                        embed.footer = EmbedBuilder.Footer()
                                        embed.footer!!.text = "Responsible moderator: ${user.asUser().username}"

                                        embeds.add(embed)
                                    }
                                }
                            }

                            bot.getLoggingExtension().logAction(
                                "Member Muted",
                                "Muted by ${user.asUser().username} for ${arguments.duration.toString()} with the reason '${arguments.reason}'",
                                member.asUser(),
                                guild!!.asGuild()
                            )

                            respond {
                                content = "Member ${arguments.user.mention} successfully muted"
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
                                content = "Cannot find that member!"
                            }
                        } else {
                            if (!member.isBot) {
                                member.dm {
                                    val embed = EmbedBuilder()
                                    embed.title = "Kicked from ${guild!!.asGuild().name}!"
                                    embed.description =
                                        "You have been kicked from ${guild!!.asGuild().name} with the reason '${arguments.reason}'"
                                    embed.footer = EmbedBuilder.Footer()
                                    embed.footer!!.text = "Responsible moderator: ${user.asUser().username}"

                                    embeds.add(embed)
                                }
                            }

                            member.kick("Kicked by ${user.asUser().username} with reason '${arguments.reason}'")

                            bot.getLoggingExtension().logAction(
                                "Member Kicked",
                                "Kicked by ${user.asUser().username} with reason '${arguments.reason}'",
                                member.asUser(),
                                guild!!.asGuild()
                            )

                            respond {
                                content = "Member ${arguments.user.mention} successfully kicked"
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
                                content = "Cannot find that member!"
                            }
                        } else {
                            if (!member.isBot) {
                                member.dm {
                                    val embed = EmbedBuilder()
                                    embed.title = "Banned from ${guild!!.asGuild().name}!"
                                    embed.description =
                                        "You have been banned from ${guild!!.asGuild().name} with the reason '${arguments.reason}'"
                                    embed.footer = EmbedBuilder.Footer()
                                    embed.footer!!.text = "Responsible moderator: ${user.asUser().username}"

                                    embeds.add(embed)
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


                            bot.getLoggingExtension().logAction(
                                "Member Banned",
                                "Banned by ${user.asUser().username} with reason '${arguments.reason}'",
                                member.asUser(),
                                guild!!.asGuild()
                            )

                            respond {
                                content = "Member ${arguments.user.mention} successfully banned"
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

                        channel.createMessage("Thread locked by a moderator")

                        bot.getLoggingExtension().logAction(
                            "Thread Locked",
                            "${channel.mention} locked by ${user.asUser().username} with reason '${arguments.reason}'",
                            user.asUser(),
                            guild!!.asGuild()
                        )

                        if (arguments.duration != null) {
                            scheduler.schedule(arguments.duration!!.seconds.toLong()) {
                                channel.edit {
                                    locked = false
                                    reason = arguments.reason
                                }

                                bot.getLoggingExtension().logAction(
                                    "Thread Unlocked",
                                    "${channel.mention} unlocked automatically after timeout",
                                    user.asUser(),
                                    guild!!.asGuild()
                                )
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

                        bot.getLoggingExtension().logAction(
                            "Channel Locked",
                            "${text.mention} locked by ${user.asUser().username} with reason '${arguments.reason}'",
                            user.asUser(),
                            guild!!.asGuild()
                        )

                        if (arguments.duration != null) {
                            scheduler.schedule(arguments.duration!!.seconds.toLong()) {
                                text.editRolePermission(guild!!.id) {
                                    speakingPermissions.forEach {
                                        allowed += it
                                    }

                                    reason = arguments.reason
                                }

                                bot.getLoggingExtension().logAction(
                                    "Channel Unlocked",
                                    "${text.mention} unlocked automatically after timeout",
                                    user.asUser(),
                                    guild!!.asGuild()
                                )
                            }
                        }
                    }
                    respond {
                        content = "Successfully locked channel"
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

                    if (channel is TextChannelThread) {
                        channel.edit {
                            locked = true
                            reason = "Thread being unlocked by ${user.mention}"
                        }

                        channel.createMessage("Thread unlocked by a moderator")

                        bot.getLoggingExtension().logAction(
                            "Thread Unlocked",
                            "${channel.mention} unlocked by ${user.asUser().username}",
                            user.asUser(),
                            guild!!.asGuild()
                        )

                        respond {
                            content = "Successfully unlocked channel"
                        }
                    } else if (channel is TextChannel) {
                        channel.editRolePermission(guild!!.id) {
                            speakingPermissions.forEach {
                                allowed += it
                            }

                            reason = "Channel being unlocked by ${user.asUser().username}"
                        }

                        channel.createMessage("Channel unlocked by a moderator")

                        bot.getLoggingExtension().logAction(
                            "Channel Unlocked",
                            "${channel.mention} unlocked by ${user.asUser().username}",
                            user.asUser(),
                            guild!!.asGuild()
                        )

                        respond {
                            content = "Successfully unlocked channel"
                        }
                    }
                }
            }
        }
        //endregion

        //region Events
        event<TextChannelThreadCreateEvent> {
            check { failIf(event.channel.member != null) }

            action {
                val author = if (event.channel.owner.id == this@ModerationExtension.kord.selfId) {
                    event.channel.messages.first().author!!
                } else {
                    event.channel.owner
                }

                if (database.config.getConfig(event.channel.guildId).moderationConfig.threadAutoJoinRoles.isNotEmpty()) {
                    val msg =
                        event.channel.createMessage("Nice thread ${author.mention}! Hold on while I get some people in here!")

                    event.channel.withTyping {
                        delay(Duration.Companion.seconds(6))
                    }

                    var mentions = ""
                    val roles = database.config.getConfig(event.channel.guildId).moderationConfig.threadAutoJoinRoles

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
        //endregion
    }

    //region Arguments
    inner class MuteArgs : SingleUserArgs() {
        val reason by string(
            "reason",
            "The reason for the kick"
        )
        val duration by optionalDuration(
            "duration",
            "The duration of the mute - optionally"
        )
    }

    inner class KickArgs : SingleUserArgs() {
        val reason by string(
            "reason",
            "The reason for the kick"
        )
    }

    inner class BanArgs : SingleUserArgs() {
        val reason by string(
            "reason",
            "The reason for the ban"
        )
        val deleteMessages by boolean(
            "delete-message-history",
            "Whether to delete the users message history"
        )
    }

    inner class LockArgs : Arguments() {
        val reason by string(
            "reason",
            "The reason for the lock"
        )
        val channel by optionalChannel(
            "channel",
            "The channel to lock, optionally"
        )
        val duration by optionalDuration(
            "duration",
            "The duration of the lock, optionally"
        )
    }

    inner class UnlockArgs : Arguments() {
        val channel by optionalChannel(
            "channel",
            "The channel to unlock, optionally"
        )
    }
    //endregion
}
